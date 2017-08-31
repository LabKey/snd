

const If = ({ cond, children }) => (
    cond ?  children() : null
);

// create permissions wrapper/s