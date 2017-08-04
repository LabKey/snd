package org.labkey.snd;

import org.labkey.api.snd.SNDPackage;
import org.labkey.api.snd.SNDService;

import java.util.Collections;
import java.util.List;

/**
 * Created by marty on 8/4/2017.
 */
public class SNDServiceImpl implements SNDService
{
    public static final SNDServiceImpl INSTANCE = new SNDServiceImpl();

    private SNDServiceImpl() {}

    public List<String> savePackage(SNDPackage pkg)
    {
        List<String> errors = Collections.emptyList();

        return errors;
    }
}
