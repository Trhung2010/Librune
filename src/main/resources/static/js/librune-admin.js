(function () {
    'use strict';

    // Mobile sidebar drawer toggle
    const sidebar = document.getElementById('adminSidebar');
    const backdrop = document.getElementById('sidebarBackdrop');
    const toggleBtn = document.getElementById('sidebarToggleBtn');

    function closeSidebar() {
        if (sidebar) sidebar.classList.remove('open');
        if (backdrop) backdrop.classList.remove('show');
    }

    if (toggleBtn && sidebar) {
        toggleBtn.addEventListener('click', function () {
            sidebar.classList.toggle('open');
            if (backdrop) backdrop.classList.toggle('show');
        });
    }
    if (backdrop) {
        backdrop.addEventListener('click', closeSidebar);
    }

    // Bootstrap client-side validation + book quantity consistency check
    document.querySelectorAll('form.needs-validation').forEach(function (form) {
        form.addEventListener('submit', function (event) {
            const totalInput = form.querySelector('input[name="totalQuantity"]');
            const availInput = form.querySelector('input[name="availableQuantity"]');
            if (totalInput && availInput) {
                const total = parseInt(totalInput.value, 10);
                const avail = parseInt(availInput.value, 10);
                if (!Number.isNaN(total) && !Number.isNaN(avail) && avail > total) {
                    availInput.setCustomValidity('Số lượng còn không được vượt quá tổng số lượng.');
                } else {
                    availInput.setCustomValidity('');
                }
            }

            if (!form.checkValidity()) {
                event.preventDefault();
                event.stopPropagation();
            }
            form.classList.add('was-validated');
        });
    });

    // Clear custom validity while typing
    document.querySelectorAll('input[name="totalQuantity"], input[name="availableQuantity"]').forEach(function (el) {
        el.addEventListener('input', function () {
            const form = el.closest('form');
            if (!form) return;
            const availInput = form.querySelector('input[name="availableQuantity"]');
            if (availInput) availInput.setCustomValidity('');
        });
    });

    // Reusable confirmation modal for destructive/status actions
    const modalEl = document.getElementById('confirmActionModal');
    let pendingForm = null;
    if (modalEl && typeof bootstrap !== 'undefined') {
        const modal = new bootstrap.Modal(modalEl);
        const msgEl = document.getElementById('confirmActionMessage');
        const confirmBtn = document.getElementById('confirmActionProceed');

        document.querySelectorAll('form[data-confirm]').forEach(function (form) {
            form.addEventListener('submit', function (e) {
                if (form.dataset.confirmed === 'true') {
                    return;
                }
                e.preventDefault();
                pendingForm = form;
                if (msgEl) {
                    msgEl.textContent = form.getAttribute('data-confirm') || 'Bạn có chắc chắn muốn thực hiện thao tác này?';
                }
                modal.show();
            });
        });

        if (confirmBtn) {
            confirmBtn.addEventListener('click', function () {
                if (pendingForm) {
                    pendingForm.dataset.confirmed = 'true';
                    modal.hide();
                    pendingForm.submit();
                    pendingForm = null;
                }
            });
        }
    }

    // Live book cover preview on book form
    const coverFileInput = document.getElementById('coverFileInput');
    const coverUrlInput = document.getElementById('coverImageInput');
    const coverPreviewImg = document.getElementById('coverPreviewImg');
    const coverPreviewEmpty = document.getElementById('coverPreviewEmpty');

    function showPreview(src) {
        if (!coverPreviewImg) return;
        if (src && src.trim() !== '') {
            coverPreviewImg.src = src;
            coverPreviewImg.classList.remove('d-none');
            if (coverPreviewEmpty) coverPreviewEmpty.classList.add('d-none');
        } else {
            coverPreviewImg.classList.add('d-none');
            if (coverPreviewEmpty) coverPreviewEmpty.classList.remove('d-none');
        }
    }

    if (coverFileInput) {
        coverFileInput.addEventListener('change', function () {
            const file = coverFileInput.files && coverFileInput.files[0];
            if (file) {
                const reader = new FileReader();
                reader.onload = function (ev) {
                    showPreview(ev.target.result);
                };
                reader.readAsDataURL(file);
            }
        });
    }

    if (coverUrlInput) {
        coverUrlInput.addEventListener('input', function () {
            if (!coverFileInput || !coverFileInput.files || coverFileInput.files.length === 0) {
                showPreview(coverUrlInput.value);
            }
        });
    }
})();
