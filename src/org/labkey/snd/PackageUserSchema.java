package org.labkey.snd;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.labkey.api.collections.CaseInsensitiveHashSet;
import org.labkey.api.collections.CaseInsensitiveTreeSet;
import org.labkey.api.data.ColumnInfo;
import org.labkey.api.data.Container;
import org.labkey.api.data.ContainerFilter;
import org.labkey.api.data.JdbcType;
import org.labkey.api.data.SQLFragment;
import org.labkey.api.data.SimpleFilter;
import org.labkey.api.data.SqlSelector;
import org.labkey.api.data.TableInfo;
import org.labkey.api.data.dialect.SqlDialect;
import org.labkey.api.exp.PropertyColumn;
import org.labkey.api.exp.property.Domain;
import org.labkey.api.exp.property.DomainProperty;
import org.labkey.api.exp.property.PropertyService;
import org.labkey.api.query.AliasedColumn;
import org.labkey.api.query.ExprColumn;
import org.labkey.api.query.FilteredTable;
import org.labkey.api.query.SchemaKey;
import org.labkey.api.query.UserSchema;
import org.labkey.api.security.User;
import org.labkey.api.snd.SNDDomainKind;
import org.labkey.api.study.StudyService;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import static org.labkey.api.query.ExprColumn.STR_TABLE_ALIAS;

public class PackageUserSchema extends UserSchema
{
    public static final String SCHEMA_NAME = "Packages";

    final SNDUserSchema _snd;

    public PackageUserSchema(SNDUserSchema parent)
    {
        super(new SchemaKey(parent.getSchemaPath(), SCHEMA_NAME), null, parent.getUser(), parent.getContainer(), parent.getDbSchema(), null);
        _snd = parent;
    }

    @Override
    public Set<String> getSchemaNames()
    {
        return Set.of();
    }

    @Override
    public Set<String> getTableNames()
    {
        List<String> list = new ArrayList<>();
        visitAll(p -> list.add(p.description));

        // check for duplicates
        Set<String> duplicates = new CaseInsensitiveHashSet();
        Set<String> ret = new CaseInsensitiveTreeSet();
        for (String s : list)
            if (!ret.add(s))
                duplicates.add(s);
        ret.removeAll(duplicates);

        return ret;
    }

    @Override
    public @Nullable TableInfo createTable(String name, ContainerFilter cf)
    {
        return createPackageTable(name);
    }

    TableInfo createPackageTable(String name)
    {
        // find domain for name
        Map<Integer,Package> pkgs = new HashMap<>();
        visitAll(p -> {
            if (name.equalsIgnoreCase(p.description))
                pkgs.put(p.packageId, p);
        });
        if (1 != pkgs.size())
            return null;
        var entry = pkgs.entrySet().iterator().next();
        int packageId = entry.getKey();
        String description = entry.getValue().description;
        TableInfo eventData = _snd.getTable("EventData", null, true, true);
        if (null == eventData)
            return null;
        return new PackageTableInfo(this, eventData, description, packageId);
    }


    class PackageTableInfo extends FilteredTable<PackageUserSchema>
    {
        final int packageId;

        PackageTableInfo(PackageUserSchema schema, TableInfo eventData, String packageName, int packageId)
        {
            super(eventData, schema, null);
            setName(packageName);
            this.packageId = packageId;

            var me = getPackage(packageId);
            if (null == me || me.superPkgIds.isEmpty())
                ((FilteredTable<?>)getRealTable()).addCondition(new SimpleFilter(new SimpleFilter.SQLClause(new SQLFragment("(0=1)"))));
            else
                ((FilteredTable<?>)getRealTable()).addInClause(eventData.getColumn("SuperPkgId"), me.superPkgIds);
        }

        /* TODO: duplicate code StudyUtils is not public (add to Study class?) */
        public static SQLFragment sequenceNumFromDateSQL(SQLFragment dateSql)
        {
            // SqlDialect.getDatePart() should not convert SQLFragment to String
            if (!dateSql.getParams().isEmpty())
                throw new IllegalStateException();
            // Returns a SQL statement that produces a single number from a date, in the form of YYYYMMDD.
            SqlDialect dialect = StudyService.get().getStudySchema().getSqlDialect();
            SQLFragment sql = new SQLFragment();
            sql.append("CAST((10000 * ").append(dialect.getDatePart(Calendar.YEAR, dateSql)).append(") + ");
            sql.append("(100 * ").append(dialect.getDatePart(Calendar.MONTH, dateSql)).append(") + ");
            sql.append("(").append(dialect.getDatePart(Calendar.DAY_OF_MONTH, dateSql)).append(") AS NUMERIC(15,4))");
            return sql;
        }

        @Override
        protected void initializeColumns()
        {
            TableInfo events = getSchema().getTable("Events");

            addColumn(new AliasedColumn(this, "SubjectId", events.getColumn("SubjectId")));
            addColumn(new AliasedColumn(this, "Date", events.getColumn("Date")));
            addColumn(new AliasedColumn(this, "QcState", events.getColumn("QcState")));
            var date = new SQLFragment(events.getColumn("Date").getValueSql(STR_TABLE_ALIAS));
            var seqnum = sequenceNumFromDateSQL(date);
            addColumn(new ExprColumn(this, "SequenceNum", seqnum, JdbcType.DECIMAL));

            wrapAllColumns(true);

            Package p = getPackage(packageId);

            Container c = getUserSchema().getContainer();
            User user = getUserSchema().getUser();
            ColumnInfo object = Objects.requireNonNull(getColumn("ObjectURI", false));
            if (p.domain != null)
            {
                for (DomainProperty dp : p.domain.getProperties())
                {
                    if (null == getColumn(dp.getName(), false))
                    {
                        PropertyColumn column = new PropertyColumn(dp.getPropertyDescriptor(), object, c, user, false);
                        addColumn(column);
                    }
                }
            }
        }

        @Override
        public @NotNull SQLFragment getFromSQL(String alias)
        {
            TableInfo events = getSchema().getTable("Events");
            return new SQLFragment("(SELECT events.SubjectId, events.Date, events.QcState, eventdata.*\n")
            .append("FROM ").append(getFromTable().getFromSQL("eventdata"))
            .append(" INNER JOIN ").append(events.getFromSQL("events"))
            .append( " ON eventdata.eventid = events.eventid\n")
            .append("WHERE events.Container=").appendValue(getContainer())
            .append(") ").append(alias).append("\n");
        }
    }


    /*
     * Package helpers
     * CONSIDER: move to a cache in SNDManager
     */

    public record SuperPkg(int superPkgId, Integer parentSuperPkgId, int pkgId, String description) {}
    public static class Package
    {
        Package(int packageId, String description, Domain d)
        {
            this.packageId = packageId;
            this.description = description;
            this.domain = d;
        }
        final int packageId;
        final String description;
        final Domain domain;
        List<Integer> superPkgIds = new ArrayList<>();
    }
    Map<Integer,Package> packagesMap;

    void initPackages()
    {
        if (null == packagesMap)
        {
            List<SuperPkg> supers = new SqlSelector(getDbSchema(), new SQLFragment(
                    new SQLFragment("SELECT SuperPkgId, ParentSuperPkgId, Pkgs.PkgId, Description FROM snd.Pkgs INNER JOIN snd.SuperPkgs ON Pkgs.PkgId = SuperPkgs.PkgId WHERE Pkgs.Container = ").appendValue(getContainer())
            )).getArrayList(SuperPkg.class);
            Map<Integer, Package> map = new HashMap<>();
            supers.forEach(superPkg -> {
                var package_ = map.computeIfAbsent(superPkg.pkgId, id ->
                {
                    String uri = SNDDomainKind.formatSndDomainURI(getContainer().getRowId(), superPkg.pkgId);
                    var domain = PropertyService.get().getDomain(getContainer(), uri);
                    return new Package(superPkg.pkgId, superPkg.description, domain);
                });
                package_.superPkgIds.add(superPkg.superPkgId);
            });
            packagesMap = map;
        }
    }

    Package getPackage(int id)
    {
        initPackages();
        return packagesMap.get(id);
    }

    void visitAll(Consumer<Package> fn)
    {
        initPackages();
        packagesMap.values().forEach(fn);
    }
}
