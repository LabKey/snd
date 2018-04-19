package org.labkey.snd.security;

public class SNDSecurityManager
{
    private static final SNDSecurityManager _instance = new SNDSecurityManager();

    public static SNDSecurityManager get()
    {
        return _instance;
    }

}
