import { Dispatch } from 'redux'

import { COOKIE_EMAIL, USER_TYPES } from './constants'
import { SignInProps, UserModel } from './model'


export function readCookie(name: string): string {
    const nameEQ = name + '=';
    const cookieVals = document.cookie.split(';');

    for (let i = 0; i < cookieVals.length; i++) {
        let c = cookieVals[i];

        while (c.charAt(0)==' ') {
            c = c.substring(1, c.length);
        }

        if (c.indexOf(nameEQ) == 0)
            return c.substring(nameEQ.length, c.length);
    }

    return null;
}

export function setCookie(name: string, value: string, days?: number) {
    let expires = '';

    // if value is null/undefined/emptyString, remove the cookie by setting the expires param to a passed date
    if (value == undefined || value == null || value.length == 0) {
        expires = '; expires=Tue, 21 Jun 2016 00:00:00 GMT';
    }
    else if (days) {
        let date = new Date();
        date.setTime(date.getTime() + (days*24*60*60*1000));
        expires = '; expires=' + date.toUTCString();
    }

    document.cookie = name + '=' + value + expires + '; path=/';
}


export function makeSignInRequest(values): Promise<any> {
    return new Promise((resolve, reject) => {
        LABKEY.Ajax.request({
            url: LABKEY.ActionURL.buildURL('login', 'loginAPI.api'),
            method: 'POST',
            jsonData: values,
            success: LABKEY.Utils.getCallbackWrapper((response) => {
                setCookie(COOKIE_EMAIL, values.remember ? values.email : null);
                resolve(response);
            }),
            failure: LABKEY.Utils.getCallbackWrapper((response) => {
                reject(response);
            }, false)
        });
    });
}

export function signInRequestAction(values: SignInProps): (dispatch: Dispatch<{}>) => Promise<any> {
    return (dispatch) => {
        return makeSignInRequest(values).then(result => {
            dispatch(successSignIn(result));

            // todo: successful signin will not correctly display in labkey header
            // until page is refreshed
            window.location.reload();
        }).catch(error => {
            // handle sign in error
        });
    };
}

export function successSignIn(response: SignInResponseProps) {
    return {
        type: USER_TYPES.SIGN_IN_SUCCESS,
        response
    };
}


// Not used yet
// function successLogout(response: any) {
//     window.location.href = LABKEY.ActionURL.buildURL('accounts', LABKEY.ActionURL.getAction());
// }
export interface SignInResponseProps {
    returnUrl: string;
    success: boolean;
    user: UserModel
}