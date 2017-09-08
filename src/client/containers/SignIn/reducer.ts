import { handleActions } from 'redux-actions';

import { USER_TYPES } from './constants';
import { UserModel } from './model';

export const user = handleActions({

    [USER_TYPES.SIGN_IN_SUCCESS]: (state: UserModel, action: any) => {
        const { response } = action;
        return Object.assign({}, state, response.user);
    }

}, new UserModel(LABKEY.user));