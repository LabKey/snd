// setAppError
import { APP_TYPES } from './constants'

export function setAppError(error) {
    return {
        type: APP_TYPES.APP_ERROR,
        error
    }
}

export function resetAppError() {
    return {
        type: APP_TYPES.APP_ERROR_RESET
    }
}