package org.labkey.snd.pipeline;

import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.labkey.api.data.Container;
import org.labkey.api.exp.ExperimentException;
import org.labkey.api.exp.Lsid;
import org.labkey.api.exp.XarContext;
import org.labkey.api.exp.api.AbstractExperimentDataHandler;
import org.labkey.api.exp.api.DataType;
import org.labkey.api.exp.api.ExpData;
import org.labkey.api.exp.api.ExpRun;
import org.labkey.api.gwt.client.model.GWTPropertyDescriptor;
import org.labkey.api.gwt.client.model.GWTPropertyValidator;
import org.labkey.api.security.User;
import org.labkey.api.snd.Package;
import org.labkey.api.snd.SNDService;
import org.labkey.api.snd.SuperPackage;
import org.labkey.api.util.FileType;
import org.labkey.api.util.XmlBeansUtil;
import org.labkey.api.util.XmlValidationException;
import org.labkey.api.view.ActionURL;
import org.labkey.api.view.ViewBackgroundInfo;
import org.labkey.data.xml.ColumnType;
import org.labkey.data.xml.PropertyValidatorType;
import org.txbiomed.snd.AttributesType;
import org.txbiomed.snd.ExportDocument;
import org.txbiomed.snd.PackageType;
import org.txbiomed.snd.PackagesType;
import org.txbiomed.snd.SuperPackageType;
import org.txbiomed.snd.SuperPackagesType;
import org.txbiomed.snd.USDACategoryType;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Binal on 8/7/2017.
 */
public class SNDDataHandler extends AbstractExperimentDataHandler
{
    private static final Logger _log = Logger.getLogger(SNDDataHandler.class);
    private static final FileType SND_INPUT = new FileType(".snd.xml");

    @Override
    public @Nullable DataType getDataType()
    {
        return null;
    }

    @Override
    public void importFile(@NotNull ExpData data, File dataFile, @NotNull ViewBackgroundInfo info, @NotNull Logger log, @NotNull XarContext context) throws ExperimentException
    {
        ExportDocument exportDocument;
        String inputFileName = dataFile.getName();

        if (SND_INPUT.isType(dataFile))
        {
            ExpRun run = data.getRun();
            if (null == run)
            {
                throw new ExperimentException("Experiment run was null for data file '" + dataFile.getName() +"'");
            }
        }

        //read xml data
        try(FileInputStream in = FileUtils.openInputStream(dataFile))
        {
            XmlOptions options = XmlBeansUtil.getDefaultParseOptions();
            options.setValidateStrict();

            //parse xml tags and get tokens/auto-generated pojos
            exportDocument = ExportDocument.Factory.parse(in, options);

            _log.info("Starting xml Validation");
            XmlBeansUtil.validateXmlDocument(exportDocument, "Validating " + inputFileName + " against schema.");
            _log.info("End xml Validation");
        }
        catch (IOException e)
        {
            throw new ExperimentException("Error reading input file '" + inputFileName +"'", e);
        }
        catch (XmlException e)
        {
            throw new ExperimentException("Could not parse input file '" + inputFileName +"'", e);
        }
        catch (XmlValidationException e)
        {
            throw new ExperimentException("Invalid XML file " + inputFileName, e);
        }

        ExportDocument.Export export = exportDocument.getExport();

        if(null != export)
        {
            parseAndSavePackages(export, info);
            parseAndSaveSuperPackages(export, info);
        }
    }

    private void parseAndSavePackages(ExportDocument.Export export, @NotNull ViewBackgroundInfo info)
    {
        //get Package nodes
        PackagesType packages = export.getPackages();
        PackageType[] packageArray = packages.getPackageArray();

        SNDService sndService = SNDService.get();

        for (PackageType packageType : packageArray)
        {
            Package pkg = parsePackage(packageType); //convert auto-generated objects/tokens to SND's Package objects
            sndService.savePackage(info.getContainer(), info.getUser(), pkg); //save to db
        }
    }

    private Package parsePackage(PackageType packageType)
    {
        Package pkg = new Package();

        //Id
        pkg.setPkgId(packageType.getId());

        //description
        pkg.setDescription(packageType.getDescription());

        //repeatable
        pkg.setRepeatable(packageType.getRepeatable());

        //displayable
        pkg.setActive(packageType.getDisplayable());

        /* extra field(s)*/
        USDACategoryType.Enum usdaCategoryVal = packageType.getUsdaCategory();
        Map<String, Object> extraFields = new HashedMap();

        //usda-category
        extraFields.put("usdaCode", usdaCategoryVal);
        pkg.setExtraFields(extraFields);

        //narrative
        pkg.setNarrative(packageType.getNarrative());

        //attributes
        pkg.setAttributes(getAttributes(packageType));

        return pkg;
    }

    private List<GWTPropertyDescriptor> getAttributes(PackageType packageType)
    {
        AttributesType attributes = packageType.getAttributes();
        ColumnType[] attributeArray = attributes.getAttributeArray();

        List<GWTPropertyDescriptor> attributesList = new LinkedList<>();
        if (attributeArray.length > 0)
        {
            for (ColumnType ct : attributeArray)
            {
                GWTPropertyDescriptor gwtpd = new GWTPropertyDescriptor();

                //columnName
                gwtpd.setName(ct.getColumnName());

                //rangeURI
                String rangeURI = ct.getRangeURI();
                if (null != rangeURI)
                    gwtpd.setRangeURI(rangeURI);
                else
                    gwtpd.setRangeURI("http://www.w3.org/2001/XMLSchema#" + ct.getDatatype());

                //nullable
                gwtpd.setRequired(ct.getNullable());

                //columnTitle
                gwtpd.setLabel(ct.getColumnTitle());

                //defaultValue
                gwtpd.setDefaultValue(ct.getDefaultValue());

                //fk
                ColumnType.Fk fk = ct.getFk();
                if(null != fk)
                {
                    gwtpd.setLookupQuery(fk.getFkTable());
                    gwtpd.setLookupSchema(fk.getFkDbSchema());
                }

                //scale
                gwtpd.setScale(ct.getScale());

                //redactedText
                gwtpd.setRedactedText(ct.getRedactedText());

                //precision
                int precision = ct.getPrecision();
                if (precision >= 1)
                {
                    StringBuilder pr = new StringBuilder("0.");
                    for (int i = 0; i < precision; i++)
                        pr.append("#");

                    gwtpd.setFormat(pr.toString());
                }

                //validator
                PropertyValidatorType validator = ct.getValidator();
                if(null != validator)
                {
                    GWTPropertyValidator gwtPropertyValidator = new GWTPropertyValidator();
                    List<GWTPropertyValidator> gwtPropertyValidatorList = new LinkedList<>();
                    gwtPropertyValidator.setName(validator.getName()); //name
                    gwtPropertyValidator.setExpression(validator.getExpression()); //expression

                    Lsid lsid = new Lsid(validator.getTypeURI());
                    gwtPropertyValidator.setType(org.labkey.api.gwt.client.model.PropertyValidatorType.getType(lsid.getObjectId()));//typeURI

                    gwtPropertyValidatorList.add(gwtPropertyValidator);
                }

                attributesList.add(gwtpd);
            }
        }
        return attributesList;
    }

    private void parseAndSaveSuperPackages(ExportDocument.Export export, @NotNull ViewBackgroundInfo info)
    {
        SuperPackagesType superPackagesType = export.getSuperPackages();
        SuperPackageType[] superPackageArray = superPackagesType.getSuperPackageArray();
        SNDService sndService = SNDService.get();
        List<SuperPackage> superPackages = new LinkedList<>();
        for(SuperPackageType superPackageType : superPackageArray)
        {
            SuperPackage superPackage = parseSuperPackage(superPackageType);
            superPackages.add(superPackage);
        }
        sndService.saveSuperPackages(info.getContainer(), info.getUser(), superPackages);
    }

    private SuperPackage parseSuperPackage(SuperPackageType superPackageType)
    {
        SuperPackage superPackage = new SuperPackage();
        superPackage.setSuperPkgId(superPackageType.getSuperPkgId());
        superPackage.setPkgId(superPackageType.getPkgId());

        return  superPackage;
    }

    @Override
    public @Nullable ActionURL getContentURL(ExpData data)
    {
        return null;
    }

    @Override
    public void deleteData(ExpData data, Container container, User user)
    {

    }

    @Override
    public void runMoved(ExpData newData, Container container, Container targetContainer, String oldRunLSID, String newRunLSID, User user, int oldDataRowID) throws ExperimentException
    {

    }

    @Override
    public Priority getPriority(ExpData data)
    {
        return (null != data && null != data.getDataFileUrl() && SND_INPUT.isType(data.getDataFileUrl()) ? Priority.MEDIUM : null);
    }
}
