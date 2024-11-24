// ユーザーのロールを取得してボタンの表示を制御
function checkUserRole() {
    fetch('/auth/role')
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to fetch user role');
            }
            return response.json();
        })
        .then(data => {
            const role = data.role;

            // ROLE_ADMIN の場合のみ管理者用ボタンを表示
            const adminControls = document.querySelectorAll('.admin-controls');
            if (role === 'ROLE_ADMIN') {
                console.log('User is ROLE_ADMIN, showing admin controls.');
                adminControls.forEach(control => {
                    control.style.display = 'block';
                });
            } else {
                console.log('User is not ROLE_ADMIN, hiding admin controls.');
                adminControls.forEach(control => {
                    control.style.display = 'none';
                });
            }
        })
        .catch(error => console.error('Error fetching user role:', error));
}

// DOMロード時にロールチェックを実行
document.addEventListener('DOMContentLoaded', checkUserRole);

