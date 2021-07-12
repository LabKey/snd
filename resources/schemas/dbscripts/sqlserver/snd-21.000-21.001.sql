UPDATE exp.PropertyValidator SET TypeURI = 'urn:lsid:labkey.com:PropertyValidator:textlength'
WHERE PropertyId IN (
    SELECT pd.PropertyId
    FROM exp.PropertyValidator pv
    JOIN exp.PropertyDescriptor pd on pd.PropertyId = pv.PropertyId
    where pv.TypeURI = 'urn:lsid:labkey.com:PropertyValidator:length' and pd.PropertyURI like '%package-snd%'
)