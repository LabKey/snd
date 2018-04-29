package org.labkey.snd.security;

public enum QCStateEnum
{
    COMPLETED("Completed", "Record has been completed and is public", true),
    REJECTED("Rejected", "Record has been reviewed and rejected", false),
    REVIEW_REQUIRED("Review Required", "Review is required prior to public release", false),
    IN_PROGRESS("In Progress", "Draft Record, not public", false);

    private String _name;
    private String _description;
    private boolean _publicData;

    QCStateEnum(String name, String description, boolean publicData)
    {
        _name = name;
        _description = description;
        _publicData = publicData;
    }

    public String getName()
    {
        return _name;
    }

    public void setName(String name)
    {
        _name = name;
    }

    public String getDescription()
    {
        return _description;
    }

    public void setDescription(String description)
    {
        _description = description;
    }

    public boolean isPublicData()
    {
        return _publicData;
    }

    public void setPublicData(boolean publicData)
    {
        _publicData = publicData;
    }
}
