// todo: create permissions wrapper/s

export function arraysMatch(arrayA: Array<string | number>, arrayB: Array<string | number>): boolean {

    if (
        arrayA.length &&
        arrayB.length &&
        arrayA.length === arrayB.length
    ) {
        let values = {};

        for (let i in arrayA) {
            values[arrayA[i]] = arrayA[i];
        }

        for (let j in arrayB) {
            if (
                typeof values[arrayB[j]] === 'undefined'
            ) {
                return false;
            }
        }

        return true;
    }

    return false;
}