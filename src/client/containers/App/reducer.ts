import { handleActions } from 'redux-actions';

import { APP_TYPES } from './constants'
import { QUERY_TYPES } from '../../query/constants'
import { PKG_WIZARD_TYPES } from '../Wizards/Packages/constants'


import { AppModel } from './model'

export const app = handleActions({

    [APP_TYPES.APP_ERROR]: (state: AppModel, action: any) => {
        const { error } = action;

        console.log('App Error', error);
        return new AppModel(Object.assign({}, state, {
            isError: true,
            message: error.exception ? error.exception : 'Something went wrong'
        }));
    },

    [APP_TYPES.APP_ERROR_RESET]: (state: AppModel, action: any) => {

        return new AppModel({
            isError: false,
            isWarning: false,
            message: undefined
        });
    },

    [APP_TYPES.APP_MESSAGE]: (state: AppModel, action: any) => {
        const { message } = action;

        return new AppModel(Object.assign({}, state, {
            message
        }));
    },

    [QUERY_TYPES.QUERY_ERROR]: (state: AppModel, action: any) => {
        const { error } = action;

        console.log('Query Error', error);
        return new AppModel(Object.assign({}, state, {
            isError: true,
            message: error.exception ? error.exception : 'Something went wrong'
        }));
    },


    [PKG_WIZARD_TYPES.PACKAGE_ERROR]: (state: AppModel, action: any) => {
        const { error } = action;

        console.log('Package Error', error);
        return new AppModel(Object.assign({}, state, {
            isError: true,
            message: error.exception ? error.exception : 'Something went wrong'
        }));
    },


}, new AppModel());