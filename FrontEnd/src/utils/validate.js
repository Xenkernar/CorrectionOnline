
const validate = (form) => {
    if (form.id !== undefined &&
        form.id.length < 1
    ) {
        return '请输入正确的学号';
    }
    if (form.password !== undefined &&
        form.password.length >20
    ) {
        return '密码长度不能超过20位';
    }
    if (form.password !== undefined &&
        form.confirmed !== undefined &&
        form.password !== form.confirmed
    ) {
        return '两次输入的密码不一致';
    }
    return null;
}

export default validate;