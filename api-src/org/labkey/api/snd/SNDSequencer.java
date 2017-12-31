package org.labkey.api.snd;

import org.labkey.api.data.Container;
import org.labkey.api.data.DbSequence;
import org.labkey.api.data.DbSequenceManager;

public enum SNDSequencer
{
    PKGID ("org.labkey.snd.api.Package", 10000),
    SUPERPKGID ("org.labkey.snd.api.SuperPackage", 1000),
    CATEGORYID ("org.labkey.snd.api.Categories", 100),
    PROJECTID ("org.labkey.snd.api.Project", 1000),
    PROJECTITEMID ("org.labkey.snd.api.ProjectItem", 20000);

    private String sequenceName;
    private int minId;
    SNDSequencer(String name, int id)
    {
        sequenceName = name;
        minId = id;
    }

    private Integer generateId(Container c)
    {
        DbSequence sequence = DbSequenceManager.get(c, sequenceName);
        sequence.ensureMinimum(minId);
        return sequence.next();
    }

    public Integer ensureId(Container container, Integer id)
    {
        if (id == null || id >= minId || id < 0)
        {
            return generateId(container);
        }

        return id;
    }
}