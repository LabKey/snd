/*
 * Copyright (c) 2017-2019 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.labkey.api.snd;

import org.labkey.api.data.Container;
import org.labkey.api.data.DbSequence;
import org.labkey.api.data.DbSequenceManager;

public enum SNDSequencer
{
    PKGID ("org.labkey.snd.api.Package", 10000),
    SUPERPKGID ("org.labkey.snd.api.SuperPackage", 10000),
    CATEGORYID ("org.labkey.snd.api.Categories", 100),
    PROJECTID ("org.labkey.snd.api.Project", 1000),
    PROJECTITEMID ("org.labkey.snd.api.ProjectItem", 30000),
    EVENTID ("org.labkey.snd.api.Event", 2000000),
    EVENTDATAID ("org.labkey.snd.api.EventData", 3500000);

    private String sequenceName;
    private int minId;

    SNDSequencer(String name, int id)
    {
        sequenceName = name;
        minId = id;
    }

    private int generateId(Container c)
    {
        DbSequence sequence = DbSequenceManager.get(c, sequenceName);
        sequence.ensureMinimum(minId);
        return Math.toIntExact(sequence.next());
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
