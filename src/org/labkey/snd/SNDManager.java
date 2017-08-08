/*
 * Copyright (c) 2017 LabKey Corporation
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

package org.labkey.snd;

import org.labkey.api.data.Container;
import org.labkey.api.data.DbSequence;
import org.labkey.api.data.DbSequenceManager;

public class SNDManager
{
    private static final SNDManager _instance = new SNDManager();
    private static final int _minPkgId = 10000;
    private static final String SND_DBSEQUENCE_NAME = "org.labkey.snd.api.SNDPackage";

    private SNDManager()
    {
        // prevent external construction with a private default constructor
    }

    public static SNDManager get()
    {
        return _instance;
    }

    public Integer generatePackageId(Container c)
    {
        DbSequence sequence = DbSequenceManager.get(c, SND_DBSEQUENCE_NAME);
        sequence.ensureMinimum(_minPkgId);
        return sequence.next();
    }
}