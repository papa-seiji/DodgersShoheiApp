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
            
            // ロールをログに表示
            console.log('Fetched role:', role);

            // ROLE_ADMIN の場合だけボタンを表示
            const mainControls = document.getElementById('main-counter-controls');
            const secondaryControls = document.getElementById('secondary-counter-controls');

            if (role === 'ROLE_ADMIN') {
                console.log('User is ROLE_ADMIN, showing buttons.');
                mainControls.style.display = 'block';
                secondaryControls.style.display = 'block';
            } else {
                console.log('User is not ROLE_ADMIN, hiding buttons.');
                mainControls.style.display = 'none';
                secondaryControls.style.display = 'none';
            }
                    })
        .catch(error => console.error('Error fetching user role:', error));
}

// DOMロード時にロールチェックを実行
document.addEventListener('DOMContentLoaded', checkUserRole);
