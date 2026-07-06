document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('.alert.alert-success, .alert.alert-danger').forEach(function (alert) {
        setTimeout(function () {
            var bsAlert = bootstrap.Alert.getOrCreateInstance(alert);
            if (bsAlert) bsAlert.close();
        }, 4000);
    });
});

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
    const el   = document.getElementById(id);
    const icon = el.querySelector('i');

    if (passed) {
        el.classList.replace('text-danger', 'text-success');
        icon.classList.replace('bi-x-circle', 'bi-check-circle');
    } else {
        el.classList.replace('text-success', 'text-danger');
        icon.classList.replace('bi-check-circle', 'bi-x-circle');
    }
}