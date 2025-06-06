🚀DodgersShoheiApp リリースノート
本アプリは、MLBの試合情報、大谷翔平の個人成績、6地区30球団の順位表、チャット機能、画像投稿機能、訪問者数の記録などを統合したWebアプリケーションです。Spring Boot (Java) をバックエンドに、PostgreSQLをデータベースに使用し、フロントエンドはHTML/CSS/JavaScriptで構築されています。
また、SSL証明書を取得し、独自ドメイン（https://letsgoohtanifromjapan.click）で運用しています。

📌実装済みの主な機能
📝 認証 & 記録
ユーザー認証：Spring Securityを利用したログイン・ログアウト機能
ログイン・ログアウト記録：login_logout_logs テーブルに記録（1日1回 8時にバッチ処理で保存＆ログファイルに出力）

📊 #MLB関連機能
MLB試合情報の取得・表示
API経由で当日の試合情報（球場、スコア、試合状況）を取得し表示
試合状況の表示
In Progress（進行中） / Final（終了） / 試合なし
チームロゴ表示：APIから取得したチーム名を元にロゴを表示
大谷翔平の個人成績の表示
試合ごとの成績（打率・本塁打・OPS など）をAPI経由で取得し表示
MLB順位表の表示
6地区30球団の順位データをAPIから取得し、チームロゴ付きで表示

💬 チャット機能
リアルタイムチャット（WebSocket）
投稿したコメントが即座に全ユーザーに反映される
基本的な機能
ユーザー名付きで投稿可能

🏆 Proud画面（画像投稿）
画像アップロード & 表示
投稿画像を proud_images テーブルに保存し、Proudページに表示
投稿機能
コメント付き画像投稿、一覧表示

いいね機能
画像に対して「いいね」ができ、ユーザーごとに記録・反映

📰 ニュース更新機能
管理者専用ニュース更新画面
news_updates テーブルにニュースを保存
記事タイトル・本文・画像をアップロード可能
管理者のみアクセス可能なインターフェースを実装

📈 訪問者数の記録
訪問者数カウント機能
visitor_counters テーブルに訪問者数を保存・表示
管理者のみ 訪問者数を閲覧可能

🔔 通知 & バッチ処理
APIのヘルスチェック
1日1回 23時 にAPIの稼働状況をログに記録
訪問者数ログのリセット
1日1回 8時 に訪問者数をリセットし、ログに記録

🎨 UI / UX 改善
ハンバーガーメニュー
オーバーレイ機能（背景を暗くし、クリックで閉じる）
スムーズなアニメーション（開閉時のフェードイン・フェードアウト）
スクロールロック（メニュー展開中はページスクロール無効）
アクセシビリティ対応（aria-expanded の設定、キーボード操作対応）
YouTube動画埋め込み
ホーム画面 に YouTube の埋め込みを追加し、動画コンテンツを提供

🛠️ 使用技術
バックエンド：Spring Boot 3.3.5 (Java)
データベース：PostgreSQL
フロントエンド：HTML / CSS / JavaScript
リアルタイム通信：WebSocket
認証：Spring Security + BCrypt
API連携：MLB API
ファイル管理：Thymeleaf（テンプレートエンジン）
デプロイ環境：AWS EC2 + Nginx + Let's Encrypt（SSL証明書取得済み）
開発ツール：VSCode, Postman
📌 バージョン管理
本アプリのソースコードは GitHub で管理しており、ブランチ運用を行いながら開発を進めています。
リリース時の履歴管理やコードのレビューもGitHub上で実施しています。

🗄️ 運用中のデータベーステーブル
本アプリでは、以下のPostgreSQLのテーブルを運用中です。

users：ユーザー情報（ID、名前、パスワード、権限 など）
comments：チャットの投稿データ（ユーザー、メッセージ、タイムスタンプ）
counters：カウンター機能の管理（管理者用の数値更新機能）
liked_users：Proudページの「いいね」機能の記録（ユーザーID、画像ID）
login_logout_logs：ログイン・ログアウトの記録（ユーザー、日時、操作内容）
mlb_api_logs：MLB APIのデータ取得ログ
mlb_yosou_datas：MLB順位予想の記録
news_updates：ニュース記事データ（タイトル、本文、画像パス など）
proud_image：Proudページの画像投稿データ
subscriptions：Webプッシュ通知のサブスクリプション情報（現在は未使用）
visitor_counter：訪問者数の記録
