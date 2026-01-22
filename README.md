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

現在は以下の構成で運用しています。

https://letsgoohtanifromjapan.click（本番運用中）  
.click ドメインは CloudFront + Function によりエッジで 301 リダイレクトされ、  
すべてのアクセスは .com ドメインへ安全かつ高速に集約されます。

https://letsgoohtanifromjapan.com  
（CloudFront 経由で本番運用）

---

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

---

🌐 ドメイン運用（Route53）

AWS Route53 にて独自ドメインを管理  
以下ドメインを運用

- letsgoohtanifromjapan.com  
- www.letsgoohtanifromjapan.com  
- letsgoohtanifromjapan.click  
- www.letsgoohtanifromjapan.click  

A レコード（Alias）により CloudFront にルーティング  
.click ドメインは誘導専用とし、CloudFront Function により  
.com ドメインへ 301 リダイレクトしています。

---

🔐 SSL / 証明書管理（ACM）

AWS Certificate Manager（ACM）にて証明書を発行  
CloudFront に証明書を集約  
HTTPS を強制（HSTS 有効）

---

☁️ CDN（CloudFront）【実運用中】

CloudFront を前段に配置した CDN 構成を採用しています。

構成  
CloudFront → ALB → Nginx → Spring Boot  

- オリジンを直接公開しない構成  
- www → apex ドメインのリダイレクトを CloudFront Functions で制御  
- 静的リソースをエッジキャッシュ  

導入効果  
- 表示速度向上  
- EC2 / ALB 負荷軽減  
- HTTPS / 証明書管理の一元化  
- グローバル配信対応  

---

⚠️ キャッシュ設計（CloudFront）

キャッシュ対象  
- HTML  
- CSS  
- JavaScript  
- 画像  

キャッシュ非対象  
- WebSocket 通信  
- 認証が必要な API  
- セッション依存の動的レスポンス  

リアルタイム性とパフォーマンスを分離した設計としています。

---

💰 コスト・請求に関する評価

CloudFront 導入後の AWS 請求は  
月額 USD 5〜6 程度。

CloudFront が通信を吸収することで  
EC2 からの直接通信量を抑制できており、  
コストパフォーマンスの高い構成となっています。

---

🗂 メディアファイル管理（Amazon S3）

画像・動画・音声は Amazon S3 に保存。

- 画像：Proud画面投稿画像、アーカイブ画像  
- 動画（mp4）：イベント演出用  
- 音声（mp3）：BGM・効果音  

EC2 にメディアを保持しない設計です。

---

🔐 セキュリティ設計

- Spring Security による認証・認可  
- 管理者専用機能の分離  
- セッション管理 / ログ記録  

---

📌 実装済みの主な機能

📝 認証 & ログ  
- ログイン / ログアウト  
- login_logout_logs テーブルへ記録（バッチ処理）

🔢 リアルタイムカウンター  
- WebSocket による即時反映  
- admin 権限のみ操作可能  
- DB 永続化

📊 MLB 関連  
- 試合情報取得  
- 大谷翔平の個人成績表示  
- 6地区30球団の順位表

🧩 スタメン / 予告先発  
- Away / Home 2カラム表示  
- 日本人選手強調表示

💬 チャット  
- WebSocket によるリアルタイム通信

🏆 Proud（画像投稿）  
- 画像投稿・一覧  
- いいね機能

📰 ニュース更新（管理者専用）

📈 訪問者数カウント

📊 MLB順位予想  
- ユーザー参加型投票  
- リアルタイム集計

🆚 選手成績比較  
- MLB Stats API 利用

🗂 アーカイブ機能

🌏 WBC コンテンツ  
- World Baseball Classic 専用ページ  
- POOL 戦（総当たり表）  
- **WBC2026 決勝トーナメント表の実装**

---

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

---

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
