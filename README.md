# DodgersShoheiApp

YouTube動画の埋め込みリンク取得方法
以下に手順をまとめます：

1. 動画のURLをコピー
YouTube 動画のページに移動し、ブラウザのアドレスバーから動画のURLをコピーします。
 例:https://www.youtube.com/watch?v=dQw4w9WgXcQ

2. 埋め込みリンクを生成
YouTube の埋め込み用URLは以下の形式になります：
https://www.youtube.com/embed/<動画ID>

動画ID は URL の v= の後に続く部分です。
例: dQw4w9WgXcQ
この例では以下のリンクが生成されます：
https://www.youtube.com/embed/dQw4w9WgXcQ

3. プレイヤーのオプションを設定（必要に応じて）
埋め込みリンクにオプションを追加することで、動画の再生方法をカスタマイズできます。以下はよく使用されるオプションの例：

autoplay=1: 動画を自動再生
mute=1: 音声をミュート
loop=1: ループ再生
controls=0: 再生コントロールを非表示

https://www.youtube.com/embed/dQw4w9WgXcQ?autoplay=1&mute=1


4. HTML に埋め込み
以下の形式で iframe タグを使用して埋め込みます：
<iframe width="560" height="315"
        src="https://www.youtube.com/embed/dQw4w9WgXcQ"
        title="YouTube video player"
        frameborder="0"
        allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
        allowfullscreen>
</iframe>

5. 動画を実際に配置する（例）
HTMLで実装例：
<div id="video-container">
    <iframe width="560" height="315"
            src="https://www.youtube.com/embed/dQw4w9WgXcQ"
            title="YouTube video player"
            frameborder="0"
            allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
            allowfullscreen>
    </iframe>
</div>

この手順を利用すれば、YouTube 動画を簡単に埋め込むことができます。CSS を調整して動画をレスポンシブにすることも可能です。必要に応じてサポートします！
