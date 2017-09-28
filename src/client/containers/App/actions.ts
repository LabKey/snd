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

export function setAppMessage(message: string) {
    return {
        type: APP_TYPES.APP_MESSAGE,
        message
    }
}