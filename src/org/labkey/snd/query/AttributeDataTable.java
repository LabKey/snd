package org.labkey.snd.query;

import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.labkey.api.data.Container;
import org.labkey.api.data.ContainerFilter;
import org.labkey.api.data.ContainerForeignKey;
import org.labkey.api.data.DbSchema;
import org.labkey.api.data.DbScope;
import org.labkey.api.data.JdbcType;
import org.labkey.api.data.SQLFragment;
import org.labkey.api.data.SqlExecutor;
import org.labkey.api.data.SqlSelector;
import org.labkey.api.dataiterator.DataIteratorBuilder;
import org.labkey.api.dataiterator.DataIteratorContext;
import org.labkey.api.exp.ObjectProperty;
import org.labkey.api.exp.OntologyManager;
import org.labkey.api.exp.OntologyObject;
import org.labkey.api.gwt.client.model.GWTPropertyDescriptor;
import org.labkey.api.query.BatchValidationException;
import org.labkey.api.query.ExprColumn;
import org.labkey.api.query.FilteredTable;
import org.labkey.api.query.InvalidKeyException;
import org.labkey.api.query.QueryForeignKey;
import org.labkey.api.query.QueryService;
import org.labkey.api.query.QueryUpdateService;
import org.labkey.api.query.QueryUpdateServiceException;
import org.labkey.api.query.SimpleQueryUpdateService;
import org.labkey.api.query.SimpleUserSchema;
import org.labkey.api.query.UserSchema;
import org.labkey.api.query.ValidationException;
import org.labkey.api.security.User;
import org.labkey.api.security.UserPrincipal;
import org.labkey.api.security.permissions.AdminPermission;
import org.labkey.api.security.permissions.Permission;
import org.labkey.api.security.permissions.ReadPermission;
import org.labkey.api.settings.AppProps;
import org.labkey.api.snd.SNDService;
import org.labkey.api.util.UnexpectedException;
import org.labkey.snd.SNDManager;
import org.labkey.snd.SNDSchema;
import org.labkey.snd.SNDUserSchema;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Exposes all of the event attribute data, one row per attribute/value combination.
 * Essentially, a specialized and filtered wrapper over exp.ObjectProperty
 */
public class AttributeDataTable extends FilteredTable<SNDUserSchema>
{
    public AttributeDataTable(@NotNull SNDUserSchema userSchema)
    {
        super(OntologyManager.getTinfoObjectProperty(), userSchema);
        setName(SNDUserSchema.TableType.AttributeData.name());
        setDescription("Event/package attribute data, one row per attribute/value combination.");

        wrapAllColumns(true);

        ExprColumn objectURI = new ExprColumn(this, "ObjectURI", new SQLFragment(ExprColumn.STR_TABLE_ALIAS + ".ObjectURI"), JdbcType.VARCHAR);
        addColumn(objectURI);

        ExprColumn eventDataAndName = new ExprColumn(this, "EventDataAndName", new SQLFragment(ExprColumn.STR_TABLE_ALIAS + ".EventDataAndName"), JdbcType.VARCHAR);
        addColumn(eventDataAndName);

        // Inject a lookup to the EventData table
        ExprColumn eventDataCol = new ExprColumn(this, "EventData", new SQLFragment(ExprColumn.STR_TABLE_ALIAS + ".EventDataId"), JdbcType.VARCHAR);
        addColumn(eventDataCol);
        eventDataCol.setFk(new QueryForeignKey(getUserSchema(), null, SNDUserSchema.TableType.EventData.name(), "EventDataId", "EventDataId", true));

        // Inject a Container column directly into the table, making it easier to follow container filtering rules
        ExprColumn containerCol = new ExprColumn(this, "Container", new SQLFragment(ExprColumn.STR_TABLE_ALIAS  + ".Container"), JdbcType.VARCHAR);
        addColumn(containerCol);
        containerCol.setFk(new ContainerForeignKey(getUserSchema()));

        getColumn("ObjectId").setFk(null);
        getColumn("PropertyId").setLabel("Property");
    }

    @Override
    protected void applyContainerFilter(ContainerFilter filter)
    {
        // Handle this in the FROM SQL generation
    }

    @NotNull
    public SQLFragment getFromSQL(String alias)
    {
        // Flatten data from primary base table (exp.ObjectProperty), exp.Object, and snd.EventData
        SQLFragment sql = new SQLFragment("(SELECT X.*, o.Container, o.ObjectURI, ed.EventDataId, CONCAT(ed.EventDataId,'-', pd.Name) as EventDataAndName FROM ");
        sql.append(super.getFromSQL("X"));
        sql.append(" INNER JOIN ");
        sql.append(OntologyManager.getTinfoObject(), "o");
        sql.append(" ON x.ObjectId = o.ObjectId AND ");
        // Apply the container filter
        sql.append(getContainerFilter().getSQLFragment(getSchema(), new SQLFragment("o.Container"), getContainer()));
        sql.append(" INNER JOIN ");
        sql.append(OntologyManager.getTinfoPropertyDescriptor(), "pd");
        // Filter to include only properties associated with packages
        sql.append(" ON x.PropertyId = pd.PropertyId AND pd.PropertyURI LIKE ? INNER JOIN ");
        // Filter to include only values associated with EventDatas
        sql.append(SNDSchema.getInstance().getTableInfoEventData(), "ed");
        sql.append(" ON ed.ObjectURI = o.ObjectURI ");
        sql.append(") ");
        sql.append(alias);

        // Note - this must be kept in sync with the PropertyURIs generated for the packages
        sql.add("urn:lsid:" + AppProps.getInstance().getDefaultLsidAuthority() + ":package-snd.Folder-%");

        return sql;
    }

    @Override
    public boolean hasPermission(@NotNull UserPrincipal user, @NotNull Class<? extends Permission> perm)
    {
        // Allow read access based on standard container permission, but for everything else require admin container access
        if (perm.equals(ReadPermission.class))
        {
            return getContainer().hasPermission(user, perm);
        }
        return getContainer().hasPermission(user, AdminPermission.class);
    }

    @Override
    public QueryUpdateService getUpdateService()
    {
        UserSchema schema = QueryService.get().getUserSchema(getUserSchema().getUser(), getContainer(), SNDSchema.NAME);
        SimpleUserSchema.SimpleTable simpleTable = new SimpleUserSchema.SimpleTable(schema, this);
        return new AttributeDataTable.UpdateService(simpleTable);
    }

    protected class UpdateService extends SimpleQueryUpdateService
    {
        private final SNDManager _sndManager = SNDManager.get();
        private final SNDService _sndService = SNDService.get();
        private final Logger _logger = Logger.getLogger(AttributeDataTable.class);
        private final DbSchema _expSchema = OntologyManager.getExpSchema();

        private Map<Integer, Object> packageMap = new HashMap();

        public UpdateService(SimpleUserSchema.SimpleTable ti)
        {
            super(ti, ti.getRealTable());

            //cache property descriptors
//            List<Package> packages = new TableSelector(SNDSchema.getInstance().getTableInfoPkgs()).getArrayList(Package.class);
            SQLFragment pkgs = new SQLFragment("select * from snd.Pkgs");
            List<Map<String, Object>> packages = (List<Map<String, Object>>) new SqlSelector(getSchema(), pkgs).getMapCollection();

            for (Map<String, Object> pkg : packages)
            {
                Integer pkgId = (Integer) pkg.get("PkgId");
                List<GWTPropertyDescriptor> packageAttributes = _sndManager.getPackageAttributes(getContainer(), ti.getUserSchema().getUser(), pkgId);
                packageMap.put(pkgId, packageAttributes);
            }
        }

        private String getObjectURI(Integer eventDataId, Container c)
        {
            return _sndManager.generateLsid(c, String.valueOf(eventDataId));
        }

        private List<Map<String, Object>> getMutableData(DataIteratorBuilder rows, DataIteratorContext context)
        {
            List<Map<String, Object>> data;
            try
            {
                data = _sndService.getMutableData(rows, context);
            }
            catch (IOException e)
            {
                return null;
            }
            return data;
        }

        private List<GWTPropertyDescriptor> getPackageAttributes(Integer pkgId)
        {
            if(packageMap.containsKey(pkgId))
                return (List<GWTPropertyDescriptor>) packageMap.get(pkgId);

            return null;
        }

        private int insertObject(Container c, String uri, List<ObjectProperty> props, Integer pkgId, int inserted, Logger logger)
        {
            try
            {
                ObjectProperty[] properties = new ObjectProperty[props.size()];
                properties = props.toArray(properties);
                OntologyManager.insertProperties(c, uri, true, properties);
                inserted += props.size();
                if (inserted % 10000 < props.size())
                    logger.info("Inserted/updated " + inserted + " rows.");
            }
            catch (ValidationException e)
            {
                logger.error(e.getMessage() + " PkgId " + pkgId, e);
                throw new UnexpectedException(e, e.getMessage() + "For PkgId: " + pkgId + ".\n");
            }

            return inserted;
        }

        private List<Map<String, Object>> updateObjectProperty(User user, Container container, List<Map<String, Object>> data,
                                         boolean isInsertOnly, boolean isUpdate, Logger logger)
        {
            logger.info("Begin updating exp.ObjectProperty.");
            final int OBJCACHE_MAX = 100;
            LinkedHashMap<String, OntologyObject> objectCache = new LinkedHashMap<String, OntologyObject>(OBJCACHE_MAX)
            {
                protected boolean removeEldestEntry(Map.Entry eldest) {
                    return size() >= OBJCACHE_MAX;
                }
            };

            int deleted = 0;
            int inserted = 0;

            String prevUri = null;
            List<ObjectProperty> prevObjProps = new ArrayList<>();
            Integer pkgId = null;

            for(Map<String, Object> row : data)
            {
                //Note: DateTimeValue is not in the source view but adding it here since it is a field in exp.ObjectProperty,
                //and it may be part of the source view in the future. May need to modify the Date type here if it doesn't work as is.
                Date dateTimeValue = (Date) row.get("DateTimeValue");

                Double floatValue = (Double) row.get("FloatValue");
                String stringValue = (String) row.get("StringValue");
                Character typeTag = ((String) row.get("TypeTag")).toCharArray()[0];
                String key = (String) row.get("_Key");

                String objectURI = getObjectURI((Integer) row.get("EventDataId"), container);
                if (prevUri == null)
                    prevUri = objectURI;

                pkgId = (Integer) row.get("PkgId");

                List<GWTPropertyDescriptor> packageAttributes = getPackageAttributes(pkgId);
                if (packageAttributes == null)
                {
                    packageAttributes = getPackageAttributes(pkgId);
                }

                if (packageAttributes != null)
                {
                    for (GWTPropertyDescriptor pd : packageAttributes)
                    {
                        if (null != pd && pd.getName().equals(key))
                        {
                            Object value = null;
                            if (floatValue != null)
                                value = floatValue;
                            else if (stringValue != null)
                                value = stringValue;
                            else if (dateTimeValue != null)
                                value = dateTimeValue;

                            ObjectProperty oprop = new ObjectProperty(objectURI, container, pd.getPropertyURI(), value);
                            oprop.setTypeTag(typeTag);
                            oprop.setPropertyId(pd.getPropertyId());

                            // Check in cache before querying db for object
                            OntologyObject ontologyObject = objectCache.get(objectURI);
                            if (ontologyObject == null)
                            {
                                ontologyObject = OntologyManager.getOntologyObject(container, objectURI);
                                objectCache.put(objectURI, ontologyObject);
                            }

                            if (null != ontologyObject)
                            {
                                if (isUpdate)
                                {
                                    OntologyManager.deleteProperty(objectURI, pd.getPropertyURI(), container, container);
                                    deleted++;
                                }
                            }

                            if (isUpdate || isInsertOnly)
                            {
                                if (!prevUri.equals(objectURI))
                                {
                                    inserted = insertObject(container, prevUri, prevObjProps, pkgId, inserted, logger);
                                    prevUri = objectURI;
                                    prevObjProps = new ArrayList<>();
                                }
                                prevObjProps.add(oprop);
                            }

                            break;
                        }
                    }
                }
            }

            if (prevObjProps.size() > 0 && pkgId != null)
            {
                inserted = insertObject(container, prevUri, prevObjProps, pkgId, inserted, logger);
            }

            logger.info("End updating exp.ObjectProperty. Deleted " + deleted + " rows. Inserted/Updated " + inserted + " rows.");

            return data;
        }

        @Override
        public int mergeRows(User user, Container container, DataIteratorBuilder rows, BatchValidationException errors,
                             @Nullable Map<Enum, Object> configParameters, Map<String, Object> extraScriptContext) throws SQLException
        {
            List<Map<String, Object>> data = getMutableData(rows, getDataIteratorContext(errors, InsertOption.MERGE, configParameters));
            return updateObjectProperty(user, container, data, false, true, ((Logger)configParameters.get(QueryUpdateService.ConfigParameters.Logger))).size();
        }

        @Override
        public int importRows(User user, Container container, DataIteratorBuilder rows, BatchValidationException errors,
                              @Nullable Map<Enum,Object> configParameters, Map<String, Object> extraScriptContext) throws SQLException
        {
            List<Map<String, Object>> data = getMutableData(rows, getDataIteratorContext(errors, InsertOption.IMPORT, configParameters));
            return updateObjectProperty(user, container, data, true, false, ((Logger)configParameters.get(QueryUpdateService.ConfigParameters.Logger))).size();
        }

        @Override
        public int truncateRows(User user, Container container, @Nullable Map<Enum, Object> configParameters, @Nullable Map<String, Object> extraScriptContext)
                throws BatchValidationException, QueryUpdateServiceException, SQLException
        {
            Logger log = null;
            if (configParameters != null)
            {
                log = ((Logger) configParameters.get(QueryUpdateService.ConfigParameters.Logger));
            }

            if (log == null)
            {
                log = _logger;
            }

            int numDeletedRows;
            String defaultLsidAuthority = AppProps.getInstance().getDefaultLsidAuthority();

            //exp.ObjectProperty has other data besides for snd - so deleting only snd relevant rows
            try (DbScope.Transaction tx = _expSchema.getScope().ensureTransaction())
            {
                SqlExecutor executor = new SqlExecutor(_expSchema);
                SQLFragment truncObjProp = new SQLFragment("delete from " + _expSchema.getName() + ".ObjectProperty\n");
                truncObjProp.append("where objectId in\n");
                truncObjProp.append("(select objectId from exp.object where objectURI like '%urn:lsid:labkey.com:SND.EventData.Folder%')\n");
                truncObjProp.append("and propertyId in\n");
                truncObjProp.append("(select propertyId from exp.propertyDescriptor where PropertyURI like '%urn:lsid:"+ defaultLsidAuthority +":package-snd.Folder%')");
                numDeletedRows = executor.execute(truncObjProp);
                tx.commit();
            }
            catch (Exception e)
            {
                log.error(e.getMessage(), e);
                throw new IllegalStateException(e);
            }
            return numDeletedRows;
        }

        @Override
        public List<Map<String, Object>> deleteRows(User user, Container container, List<Map<String, Object>> oldRows,
                                                    @Nullable Map<Enum, Object> configParameters, @Nullable Map<String, Object> extraScriptContext)
                throws InvalidKeyException, BatchValidationException, QueryUpdateServiceException, SQLException
        {
            Logger log = null;
            if (configParameters != null)
            {
                log = ((Logger) configParameters.get(QueryUpdateService.ConfigParameters.Logger));
            }

            if (log == null)
            {
                log = _logger;
            }

            try (DbScope.Transaction tx = _expSchema.getScope().ensureTransaction())
            {
                for (Map<String, Object> row : oldRows)
                {
                    Integer objectId = (Integer) row.get("ObjectId");
                    Integer propertyId = (Integer) row.get("PropertyId");

                    SqlExecutor executor = new SqlExecutor(_expSchema);
                    SQLFragment deleteObjProp = new SQLFragment("delete from " + _expSchema.getName() + ".ObjectProperty\n");
                    deleteObjProp.append("where objectId = ?\n");
                    deleteObjProp.add(objectId);
                    deleteObjProp.append("and propertyId = ?");
                    deleteObjProp.add(propertyId);
                    executor.execute(deleteObjProp);
                    log.info("Deleting a row in exp.ObjectProperty with objectId = " + objectId + ", and propertyId = " + propertyId);
                }
                tx.commit();
            }
            catch (Exception e)
            {
                log.error(e.getMessage(), e);
                throw new IllegalStateException(e);
            }
            return oldRows;
        }
    }
}