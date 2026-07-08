function togglePassword(fieldId, btn) {
    const field = document.getElementById(fieldId);
    const icon  = btn.querySelector('i');
    if (field.type === 'password') {
        field.type = 'text';
        icon.classList.replace('bi-eye', 'bi-eye-slash');
    } else {
        field.type = 'password';
        icon.classList.replace('bi-eye-slash', 'bi-eye');
    }
}

function validatePassword(value) {
    check('chk-length',  value.length >= 8);
    check('chk-upper',   /[A-Z]/.test(value));
    check('chk-lower',   /[a-z]/.test(value));
    check('chk-digit',   /[0-9]/.test(value));
    check('chk-special', /[@$!%*?&]/.test(value));
}

function check(id, passed) {
    const el = document.getElementById(id);
    if (!el) return;
    const icon = el.querySelector('i');
    if (passed) {
        el.classList.add('pass');
        icon.classList.replace('bi-x-circle-fill', 'bi-check-circle-fill');
    } else {
        el.classList.remove('pass');
        icon.classList.replace('bi-check-circle-fill', 'bi-x-circle-fill');
    }
}
