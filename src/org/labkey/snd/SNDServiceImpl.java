package org.labkey.snd;

import org.labkey.api.data.Container;
import org.labkey.api.data.DbScope;
import org.labkey.api.data.TableInfo;
import org.labkey.api.query.BatchValidationException;
import org.labkey.api.query.DuplicateKeyException;
import org.labkey.api.query.InvalidKeyException;
import org.labkey.api.query.QueryUpdateService;
import org.labkey.api.query.QueryUpdateServiceException;
import org.labkey.api.query.ValidationException;
import org.labkey.api.security.User;
import org.labkey.api.snd.SNDPackage;
import org.labkey.api.snd.SNDService;
import org.labkey.api.util.UnexpectedException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by marty on 8/4/2017.
 */
public class SNDServiceImpl implements SNDService
{
    public static final SNDServiceImpl INSTANCE = new SNDServiceImpl();

    private SNDServiceImpl() {}

    @Override
    public void savePackage(Container c, User u, SNDPackage pkg)
    {
        TableInfo pkgsTable = SNDSchema.getInstance().getTableInfoPkgs();
        QueryUpdateService qus = pkgsTable.getUpdateService();
        if (qus == null)
            throw new IllegalStateException();

        BatchValidationException errors = new BatchValidationException();
        Map<String, Object> pkgValues = new HashMap<>();
        pkgValues.put("Description", pkg.getDescription());
        pkgValues.put("Active", pkg.isActive());
        pkgValues.put("Repeatable", pkg.isRepeatable());
        pkgValues.put("Container", c);

        List<Map<String,Object>> rows = new ArrayList<>();
        rows.add(pkgValues);

        // Create/update package in pkgs table
        if(null != pkg.getPkgId() && pkg.getPkgId() > 0)
        {
            pkgValues.put("PkgId", pkg.getPkgId());
            try (DbScope.Transaction tx = pkgsTable.getSchema().getScope().ensureTransaction())
            {
                qus.updateRows(u, c, rows, null, null, null);
                tx.commit();
            }
            catch (QueryUpdateServiceException | BatchValidationException | InvalidKeyException | SQLException e)
            {
                errors.addRowError(new ValidationException(e.getMessage()));
            }

            if (errors.hasErrors())
                throw new UnexpectedException(errors);
        }
        else
        {
            pkg.setPkgId(SNDManager.get().generatePackageId(c));
            pkgValues.put("PkgId", pkg.getPkgId());

            try (DbScope.Transaction tx = pkgsTable.getSchema().getScope().ensureTransaction())
            {
                qus.insertRows(u, c, rows, errors, null, null);
                tx.commit();
            }
            catch (QueryUpdateServiceException | BatchValidationException | DuplicateKeyException | SQLException e)
            {
                errors.addRowError(new ValidationException(e.getMessage()));
            }

            if (errors.hasErrors())
                throw new UnexpectedException(errors);
        }
    }
}
