DodgersShoheiApp

本アプリは 個人開発プロジェクトとして、
設計・実装・インフラ構築・本番運用までを一貫して行っている Web アプリケーションです。

🚀 DodgersShoheiApp リリースノート
本アプリは、MLBの試合情報、大谷翔平の個人成績、
6地区30球団の順位表、チャット機能、画像投稿機能、訪問者数の記録などを統合した Web アプリケーションです。

バックエンド：Spring Boot（Java）
データベース：PostgreSQL
フロントエンド：HTML / CSS / JavaScript
リアルタイム通信：WebSocket
SSL 証明書を取得し、独自ドメイン + HTTPS で本番運用しています。

現在は
https://letsgoohtanifromjapan.click（本番運用中）
https://letsgoohtanifromjapan.com（CloudFront
 経由で運用開始）

の構成となっています。


🏗️ インフラ・運用構成（全体像）
[ Browser ]
     ↓
[ CloudFront ]
     ↓
[ Application Load Balancer ]
     ↓
[ Nginx ]
     ↓
[ Spring Boot Application ]
     ↓
[ PostgreSQL ]


🌐 ドメイン運用（Route53）
AWS Route53 にて独自ドメインを管理
以下ドメインを運用
letsgoohtanifromjapan.com
www.letsgoohtanifromjapan.com
letsgoohtanifromjapan.click
www.letsgoohtanifromjapan.click
A レコード（Alias）により CloudFront にルーティング


🔐 SSL / 証明書管理（ACM）
AWS Certificate Manager（ACM）にて証明書を発行
CloudFront に証明書を集約
HTTPS を強制（HSTS 有効）


☁️ CDN（CloudFront）【実運用中】
パフォーマンス向上・セキュリティ強化・通信コスト最適化を目的として
AWS CloudFront を前段に配置した CDN 構成を実運用しています。

構成
CloudFront → ALB → Nginx → Spring Boot
オリジンを直接公開せず、CloudFront 経由のアクセスに限定
www → apex ドメインのリダイレクトは CloudFront Functions で制御
CloudFront 導入による効果
静的リソースのエッジキャッシュによる 表示速度向上
EC2 / ALB へのリクエスト削減による 負荷軽減
HTTPS / 証明書管理の一元化
グローバル配信とセキュリティ向上


⚠️ キャッシュ設計（CloudFront）
キャッシュ対象
HTML
CSS
JavaScript
画像（img）
キャッシュ非対象
WebSocket 通信
認証が必要な API
セッション依存の動的レスポンス
リアルタイム性が求められる機能と、
パフォーマンス重視の静的配信を分離した設計としています。


💰 コスト・請求に関する評価
CloudFront 導入後の AWS 請求を確認した結果、
月額 USD 5〜6 程度
主な課金要素は Data Transfer
CloudFront / ALB / ACM / Route53 は想定通り低コスト
CloudFront が前段で通信を吸収することで、
EC2 からの直接インターネット通信量を抑制できており、
コストパフォーマンスの良い構成であることを確認しています。


🗂 メディアファイル管理（Amazon S3）
画像・動画・音声などのメディアファイルは Amazon S3 に保存しています。
画像：Proud画面投稿画像、アーカイブ画像
動画（mp4）：イベント・演出用動画
音声（mp3）：BGM・効果音
EC2 にメディアファイルを直接保持せず、
将来的には S3 + CloudFront 経由での配信を前提とした設計です。


🔐 セキュリティ設計
Spring Security による認証・認可
エンドポイント単位のアクセス制御
管理者専用機能の分離
セッション管理 / ログ記録


📌 実装済みの主な機能

📝 認証 & ログ
ログイン / ログアウト機能
login_logout_logs テーブルへ記録（1日1回バッチ処理）

🔢 リアルタイムカウンター
WebSocket による即時反映
admin 権限ユーザーのみ操作可能
DB 永続化（再起動後も保持）

📊 MLB 関連機能
試合情報取得・表示
大谷翔平の個人成績表示
6地区30球団の順位表表示

🧩 スタメン / 予告先発表示
Away / Home の2カラム構成
日本人選手強調表示

💬 チャット機能
WebSocket によるリアルタイムチャット

🏆 Proud（画像投稿）
画像投稿・一覧表示

いいね機能
📰 ニュース更新（管理者専用）
記事作成・画像アップロード

📈 訪問者数カウント
visitor_counter テーブルで管理

📊 MLB順位予想
ユーザー参加型投票
リアルタイム集計

🆚 選手成績比較
MLB Stats API を利用

🗂 アーカイブ機能
過去イベント・試合のメディア閲覧

🌏 WBC コンテンツ
World Baseball Classic 専用ページ


🛠 使用技術
Spring Boot 3.3.5
Java
PostgreSQL
HTML / CSS / JavaScript
WebSocket
Spring Security + BCrypt
MLB Stats API
AWS（EC2 / ALB / CloudFront / Route53 / ACM / S3）
Nginx
GitHub


🗄️ 運用中のデータベーステーブル
users
comments
counters
liked_users
login_logout_logs
mlb_api_logs
mlb_yosou_datas
news_updates
proud_image
subscriptions
visitor_counter