export const emailValidator: (v?: string) => string = (value: string) => {
    return value && /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,4}$/i.test(value)
        ? undefined
        : 'Invalid email address';
};

export const requiredValidator: (v?: string) => string = (value?: string) => {
    return value ? undefined : 'Required';
};
