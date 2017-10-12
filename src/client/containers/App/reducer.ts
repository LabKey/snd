import { handleActions } from 'redux-actions';

import { APP_TYPES } from './constants'
import { QUERY_TYPES } from '../../query/constants'
import { PKG_WIZARD_TYPES } from '../Wizards/Packages/constants'


import { AppModel } from './model'

export const app = handleActions({

    [APP_TYPES.APP_ERROR]: setErrorMessages,

    [APP_TYPES.APP_ERROR_RESET]: (state: AppModel, action: any) => {
        const { id } = action;

        const messages = state.messages.filter(msg => {
            return msg.id !== id;
        });

        return new AppModel(Object.assign({}, state, {
            isError: messages.length > 0,
            isWarning: false,
            messages
        }));
    },

    [APP_TYPES.APP_MESSAGE]: (state: AppModel, action: any) => {
        const { message } = action;
        let messages = [].concat(state.messages);

        messages.push({
            id: state.messages.length,
            message,
            role: 'success'
        });

        return new AppModel(Object.assign({}, state, {
            messages
        }));
    },

    [APP_TYPES.APP_CLEAR_MESSAGE]: (state: AppModel, action: any) => {
        const { id, message } = action.values;

        const messages = state.messages.filter(msg => {
            return msg.id !== id && msg.message !== message;
        });

        return new AppModel(Object.assign({}, state, {
            messages
        }));
    },

    [QUERY_TYPES.QUERY_ERROR]: setErrorMessages,


    [PKG_WIZARD_TYPES.PACKAGE_ERROR]: setErrorMessages,


}, new AppModel());

// This functionality is shared for multiple actions listened to by this reducer
function setErrorMessages(state: AppModel, action: any) {
    const { error } = action;

    const message = error.exception ? error.exception : 'Something went wrong';
    let messages = [].concat(state.messages);

    messages.push({
        id: state.messages.length,
        message,
        role: 'error'
    });

    return new AppModel(Object.assign({}, state, {
        isError: true,
        messages
    }));
}