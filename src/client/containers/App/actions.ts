// setAppError
import { APP_TYPES } from './constants'

interface ClearMessage {
    message?: string
    id?: number
}

export function clearAppMessage(values: ClearMessage) {
    return {
        type: APP_TYPES.APP_CLEAR_MESSAGE,
        values
    }
}

export function resetAppError(id?: number) {
    return {
        type: APP_TYPES.APP_ERROR_RESET,
        id
    }
}

export function setAppError(error) {
    return {
        type: APP_TYPES.APP_ERROR,
        error
    }
}

export function setAppMessage(message: string) {
    return {
        type: APP_TYPES.APP_MESSAGE,
        message
    }
}