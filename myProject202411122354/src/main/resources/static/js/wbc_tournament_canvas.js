document.addEventListener("DOMContentLoaded", async () => {
  const canvas = document.getElementById("wbc-tournament-canvas");
  if (!canvas) return;

// ===== 拡大表示用 Canvas =====
const previewCanvas = document.getElementById("box-preview-canvas");
const previewCtx = previewCanvas.getContext("2d");

const ctx = canvas.getContext("2d"); // ★ 必須

  // ==============================
  // DBデータ取得
  // ==============================
  const res = await fetch("/api/wbc/tournament?year=2026");
  const matches = await res.json();

  const tournament = {};
  matches.forEach(m => {
    if (!tournament[m.round]) tournament[m.round] = {};
    tournament[m.round][m.matchNo] = m;
  });

  // ==============================
  // Canvas設定
  // ==============================
  canvas.width = 1100;
  canvas.height = 1200;

  ctx.strokeStyle = "#000";
  ctx.lineWidth = 2;
  ctx.textAlign = "center";
  ctx.textBaseline = "middle";


// ==============================
// 国旗 Image キャッシュ（ちらつかない方式）
// ==============================
const FLAGS = (() => {
  const srcMap = {
    // Group A / 強豪
    USA: "https://dodgersshoheiapp-assets.s3.us-east-1.amazonaws.com/img/2026_WBC/flags/us.jpg",
    JAPAN: "https://dodgersshoheiapp-assets.s3.us-east-1.amazonaws.com/img/2026_WBC/flags/jp.jpg",
    KOREA: "https://dodgersshoheiapp-assets.s3.us-east-1.amazonaws.com/img/2026_WBC/flags/kr.jpg",
    AUSTRALIA: "https://dodgersshoheiapp-assets.s3.us-east-1.amazonaws.com/img/2026_WBC/flags/au.jpg",
    CANADA: "https://dodgersshoheiapp-assets.s3.us-east-1.amazonaws.com/img/2026_WBC/flags/ca.jpg",
    MEXICO: "https://dodgersshoheiapp-assets.s3.us-east-1.amazonaws.com/img/2026_WBC/flags/mx.jpg",

    // 中南米
    BRAZIL: "https://dodgersshoheiapp-assets.s3.us-east-1.amazonaws.com/img/2026_WBC/flags/br.jpg",
    ITALY: "https://dodgersshoheiapp-assets.s3.us-east-1.amazonaws.com/img/2026_WBC/flags/it.jpg",
    "PUERTO RICO": "https://dodgersshoheiapp-assets.s3.us-east-1.amazonaws.com/img/2026_WBC/flags/pr.jpg",
    CUBA: "https://dodgersshoheiapp-assets.s3.us-east-1.amazonaws.com/img/2026_WBC/flags/cu.jpg",
    COLOMBIA: "https://dodgersshoheiapp-assets.s3.us-east-1.amazonaws.com/img/2026_WBC/flags/co.jpg",
    PANAMA: "https://dodgersshoheiapp-assets.s3.us-east-1.amazonaws.com/img/2026_WBC/flags/pa.jpg",
    VENEZUELA: "https://dodgersshoheiapp-assets.s3.us-east-1.amazonaws.com/img/2026_WBC/flags/ve.jpg",
    "DOMINICAN REPUBLIC": "https://dodgersshoheiapp-assets.s3.us-east-1.amazonaws.com/img/2026_WBC/flags/do.jpg",

    // 欧州・その他
    NETHERLANDS: "https://dodgersshoheiapp-assets.s3.us-east-1.amazonaws.com/img/2026_WBC/flags/nl.jpg",
    ISRAEL: "https://dodgersshoheiapp-assets.s3.us-east-1.amazonaws.com/img/2026_WBC/flags/il.jpg",
    NICARAGUA: "https://dodgersshoheiapp-assets.s3.us-east-1.amazonaws.com/img/2026_WBC/flags/ni.jpg",
    CZECH_REPUBLIC: "https://dodgersshoheiapp-assets.s3.us-east-1.amazonaws.com/img/2026_WBC/flags/cz.jpg",
    "CHINESE TAIPEI": "https://dodgersshoheiapp-assets.s3.us-east-1.amazonaws.com/img/2026_WBC/flags/tw.jpg",
    "UNITED KINGDOM": "https://dodgersshoheiapp-assets.s3.us-east-1.amazonaws.com/img/2026_WBC/flags/gb.jpg"
  };

  const images = {};
  Object.keys(srcMap).forEach(k => {
    const img = new Image();
    img.onload = () => redraw(); // ★ロード完了で再描画（既存仕様）
    img.src = srcMap[k];
    images[k] = img;
  });

  return images;
})();

// ==============================
// 共通描画関数（影なし・安定版）
// ==============================
function drawBox(ctx, x, y, w, h, title, lines = []) {


  // ② 枠線
  ctx.strokeStyle = "#000";
  ctx.lineWidth = 2;
  ctx.strokeRect(x, y, w, h);

  //⑤ 影付き BOX（おすすめ・高級感）
  ctx.save();

  ctx.shadowColor = "rgba(9, 9, 9, 0.51)";
  ctx.shadowBlur = 10;
  ctx.shadowOffsetX = 14;
  ctx.shadowOffsetY = 9;

  // BOX 背景
  ctx.fillStyle = "#d1d1d1";
  ctx.fillRect(x, y, w, h);

  ctx.shadowColor = "transparent"; // 枠に影をつけない
  ctx.strokeStyle = "#000";
  ctx.strokeRect(x, y, w, h);

  ctx.restore();  

  // ③ タイトル
  ctx.font = "bold 14px sans-serif";
  ctx.fillStyle = "#000";
  ctx.textAlign = "center";
  ctx.textBaseline = "middle";
  ctx.fillText(title, x + w / 2, y + 18);

  // ④ 中身テキスト
  ctx.font = "12px sans-serif";
  lines.forEach((t, i) => {
    if (t != null) {
      ctx.fillText(t, x + w / 2, y + 45 + i * 18);
    }
  });
}


function vLine(x, y1, y2) {
  ctx.beginPath();
  ctx.moveTo(x, y1);
  ctx.lineTo(x, y2);
  ctx.stroke();
}

function hLine(x1, x2, y) {
  ctx.beginPath();
  ctx.moveTo(x1, y);
  ctx.lineTo(x2, y);
  ctx.stroke();
}

  // ==============================
  // 試合表示行生成
  // ==============================
  function matchLines(m) {
    if (!m) return ["未定", "vs", "未定"];

    const homeScore = m.homeScore != null ? m.homeScore : "";
    const awayScore = m.awayScore != null ? m.awayScore : "";

    return [
      `${m.homeTeam}　${homeScore}`,
      "vs",
      `${m.awayTeam}　${awayScore}`,
      m.winnerTeam ? `勝者：${m.winnerTeam}` : null
    ];
  }


  // ==============================
// 表示用チーム文字列生成（非表示対応・確定版）
// ==============================
function buildTeamText(team, score) {
  // チーム未確定
  if (!team) return "未定";

  // スコア未入力（null / undefined）の場合は表示しない
  if (score === null || score === undefined) {
    return team;
  }

  // 0 も含めて表示する
    return `${team} ${score}`;
}




function buildPreviewLines(data) {
  if (!data) return ["未定"];

  const lines = [];

  if (data.homeTeam) {
    lines.push(buildTeamText(data.homeTeam, data.homeScore));
  }

  lines.push("vs");

  if (data.awayTeam) {
    lines.push(buildTeamText(data.awayTeam, data.awayScore));
  }

  if (data.winnerTeam) {
    lines.push(`Win : ${data.winnerTeam}`);
  }

  return lines;
}




// ==============================
// 国旗＋チーム名（左寄せ）【最終安定版】
// ==============================
function drawTeamWithFlagLeft(x, y, teamText) {
  if (!teamText) return;

  ctx.font = "12px sans-serif";
  ctx.textAlign = "left";

  // ★ 空白・全角・NBSP をすべて正規化
  const normalized = teamText
    .replace(/\u00A0/g, " ") // NBSP
    .replace(/\s+/g, " ")
    .trim();

  // ★ 最後が数字ならスコアとして分離
  const match = normalized.match(/^(.*?)(?:\s+(\d+))?$/);
  const team = match?.[1]?.trim();
  const score = match?.[2] ?? "";

  const flag = FLAGS[team];
  const FLAG_W = 26;
  const FLAG_H = 18;
  const GAP = 6;

  if (flag && flag.complete && flag.naturalWidth > 0) {
    ctx.drawImage(flag, x, y - FLAG_H / 2, FLAG_W, FLAG_H);
    ctx.fillText(`${team}${score ? " " + score : ""}`, x + FLAG_W + GAP, y);
  } else {
    // フォールバック（必ず文字は出す）
    ctx.fillText(normalized, x, y);
  }
}



// ==============================
// 国旗＋チーム名（中央揃え）
// ==============================
function drawTeamWithFlagCentered(cx, y, teamText) {
  if (!teamText) return;

  ctx.font = "12px sans-serif";
  ctx.textAlign = "left"; // ★手動配置するので left 固定

  // teamText: "USA 7" / "PUERTO RICO 3" など
  const parts = teamText.split(" ");
  const team = parts.slice(0, -1).join(" "); // 複数単語対応
  const score = parts[parts.length - 1];

  const text = `${team} ${score}`;

  const flag = FLAGS[team];
  const FLAG_W = flag ? 26 : 0;
  const FLAG_H = 18;
  const GAP = flag ? 6 : 0;

  // テキスト幅
  const textWidth = ctx.measureText(text).width;

  // 全体幅（国旗 + gap + テキスト）
  const totalWidth = FLAG_W + GAP + textWidth;

  // 左端X（中央から逆算）
  const startX = cx - totalWidth / 2;

  // 国旗
  if (flag && flag.complete && flag.naturalWidth > 0) {
    ctx.drawImage(flag, startX, y - FLAG_H / 2, FLAG_W, FLAG_H);
  }

  // テキスト
  ctx.fillText(text, startX + FLAG_W + GAP, y);
}


// ==============================
// ラウンドタイトル（中央寄せ）
// ==============================
function drawRoundTitleCentered(cx, y, text) {
  if (!text) return;

  ctx.font = "bold 14px sans-serif";
  ctx.textAlign = "center";
  ctx.fillText(text, cx, y);
}


// ==============================
// 勝者（国旗＋中央揃え）
// ==============================
function drawWinnerWithFlagCentered(cx, y, winnerTeam) {
  if (!winnerTeam) return;
  drawTeamWithFlagCentered(cx, y, winnerTeam);
}

// ==============================
// 勝者（国旗＋Win表記・中央揃え）
// ==============================
function drawWinnerWithFlagCentered(cx, y, winnerText) {
  if (!winnerText) return;

  ctx.font = "bold 12px sans-serif";
  ctx.textAlign = "left";

//     ctx.save();                 // ★ 追加
//   ctx.textAlign = "center";   // font は外から渡す
//   ctx.fillText(text, cx, y);
//   ctx.restore();              // ★ 追加

  // "Win：COLOMBIA" / "Win: COLOMBIA" 両対応
  const normalized = winnerText
    .replace("Win：", "")
    .replace("Win:", "")
    .trim();

  const team = normalized;

  const label = "Win : ";
  const flag = FLAGS[team];

  const FLAG_W = flag ? 26 : 0;
  const FLAG_H = 18;
  const GAP = flag ? 6 : 0;

  const labelWidth = ctx.measureText(label).width;
  const teamWidth  = ctx.measureText(team).width;

  const totalWidth = labelWidth + FLAG_W + GAP + teamWidth;

  const startX = cx - totalWidth / 2;

  // Win :
  ctx.fillText(label, startX, y);

  // 国旗
  if (flag && flag.complete && flag.naturalWidth > 0) {
    ctx.drawImage(
      flag,
      startX + labelWidth,
      y - FLAG_H / 2,
      FLAG_W,
      FLAG_H
    );
  }

  // チーム名
  ctx.fillText(
    team,
    startX + labelWidth + FLAG_W + GAP,
    y
  );
}


  // ==============================
  // レイアウト
  // ==============================
  const CENTER_X = 550;
  const BOX_W = 210;
  const BOX_H = 135;
  const TOP_MARGIN = 60;

  const Y_CHAMP = TOP_MARGIN + 60;
  const Y_FINAL = TOP_MARGIN + 200;
  const Y_SF    = TOP_MARGIN + 400;
  const Y_QF    = TOP_MARGIN + 590;

  const GAP_X = 280;
  const CONNECT = 30;

  // ==============================
  // 再描画本体（★唯一の追加構造）
  // ==============================

  // ★ 追加（1回だけ）
const BOX_HIT_AREAS = [];

canvas.addEventListener("click", (e) => {
  const rect = canvas.getBoundingClientRect();

  const scaleX = canvas.width  / rect.width;
  const scaleY = canvas.height / rect.height;

  const x = (e.clientX - rect.left) * scaleX;
  const y = (e.clientY - rect.top)  * scaleY;

  const hit = BOX_HIT_AREAS.find(b =>
    x >= b.x &&
    x <= b.x + b.w &&
    y >= b.y &&
    y <= b.y + b.h
  );

  if (!hit) return;

  console.log("HIT:", hit.title, hit.data);
  openModal(hit);
});



function drawBoxPreview(hit, canvas, ctx) { //////////////////////////////////////////調整箇所
  const scale = 1.3;

  const w = BOX_W * scale;
  const h = BOX_H * scale;

  ctx.clearRect(0, 0, canvas.width, canvas.height);

  drawBox(
    ctx,
    (canvas.width - w) / 2,
    (canvas.height - h) / 2,
    w,
    h,
    hit.title,
    buildPreviewLines(hit.data)
  );
}



function openModal(hit) {
  const modal = document.getElementById("box-modal");
  modal.classList.remove("hidden");

  const previewCanvas = document.getElementById("box-preview-canvas");
  if (!previewCanvas) return;

  const previewCtx = previewCanvas.getContext("2d");

  // ★ Canvasサイズを明示（重要）
  previewCanvas.width = 300   //////////////////////////////////////////調整箇所
  previewCanvas.height = 250; //////////////////////////////////////////調整箇所

  drawBoxPreview(hit, previewCanvas, previewCtx); // ← ★ 呼ぶ！！
}


document.getElementById("modal-close").onclick = () => {
  document.getElementById("box-modal").classList.add("hidden");
};

/* 👇 ここに追加するのが正解 */
const overlay = document.getElementById("modal-overlay");
if (overlay) {
  overlay.onclick = () => {
    document.getElementById("box-modal").classList.add("hidden");
  };
}

  function redraw() {
    ctx.clearRect(0, 0, canvas.width, canvas.height);

  // ★ 必須：当たり判定を毎回リセット
  BOX_HIT_AREAS.length = 0;



// ==============================
// 優勝（CHAMPION）※ FINAL と同一思想
// ==============================
const champion = tournament.FINAL?.[1]?.winnerTeam;

// 枠のみ描画（文字は描かない）
drawBox(
    ctx,
  CENTER_X - BOX_W / 2,
  Y_CHAMP,
  BOX_W,
  BOX_H,
  "",
  [
    "",
    "",
    "",
    null
  ]
);

BOX_HIT_AREAS.push({
  x: CENTER_X - BOX_W / 2,
  y: Y_CHAMP,
  w: BOX_W,
  h: BOX_H,
  title: "優勝",
  data: { winnerTeam: champion }  // 修正
});


// タイトル（中央寄せ）
drawRoundTitleCentered(
  CENTER_X,
  Y_CHAMP + 18,
  "優勝"
);

// 勝者表示（国旗付き・中央寄せ・強調）
if (champion) {
  ctx.save(); // ★ 他への影響防止

  ctx.font = "bold 100px sans-serif"; // ← 太字＋少し大きく
  drawWinnerWithFlagCentered(
    CENTER_X,
    Y_CHAMP + 72, // ★ 少し下げてバランス調整
    champion
  );

  ctx.restore();
} else {
  ctx.font = "12px sans-serif";
  ctx.textAlign = "center";
  ctx.fillText("未定", CENTER_X, Y_CHAMP + 70);
}

// ==============================
// 決勝（FINAL）※ 準決勝と同一思想
// ==============================
const final = tournament.FINAL?.[1];

// 枠だけ描画（中身は描かない）
drawBox(
    ctx,
  CENTER_X - BOX_W / 2,
  Y_FINAL,
  BOX_W,
  BOX_H,
  "",
  [
    "",
    "vs",
    "",
    null
  ]
);

BOX_HIT_AREAS.push({
  x: CENTER_X - BOX_W / 2,
  y: Y_FINAL,
  w: BOX_W,
  h: BOX_H,
  title: "決勝",
  data: tournament.FINAL?.[1]
});

// タイトル＋日付（中央寄せ・一体表記）
drawRoundTitleCentered(
  CENTER_X,
  Y_FINAL + 18,
  "決勝 '26/3/17(火)"
);

// チーム表示
if (final) {
    drawTeamWithFlagCentered(
    CENTER_X,
    Y_FINAL + 45,
    buildTeamText(final.homeTeam, final.homeScore)
    );

    drawTeamWithFlagCentered(
    CENTER_X,
    Y_FINAL + 80,
    buildTeamText(final.awayTeam, final.awayScore)
    );
}

// 勝者表示
if (final?.winnerTeam) {
  drawWinnerWithFlagCentered(
    CENTER_X,
    Y_FINAL + 110,
    `Win：${final.winnerTeam}`
  );
}

// 上位（優勝）との接続線は既存を維持
vLine(CENTER_X, Y_CHAMP + BOX_H, Y_FINAL);



    // 準決勝
    const SF_LEFT_X  = CENTER_X - GAP_X - BOX_W / 2;
    const SF_RIGHT_X = CENTER_X + GAP_X - BOX_W / 2;

// ---------- 準決勝① ----------
const sf1 = tournament.SF?.[1];

drawBox(
  ctx,
  SF_LEFT_X,
  Y_SF,
  BOX_W,
  BOX_H,
  "",
  [
    "",
    "vs",
    "",
    null
  ]
);

BOX_HIT_AREAS.push({
  x: SF_LEFT_X,
  y: Y_SF,
  w: BOX_W,
  h: BOX_H,
  title: "① 準決勝",
  data: sf1
});

drawRoundTitleCentered(
  SF_LEFT_X + BOX_W / 2,
  Y_SF + 18,
  "① 準決勝 '26/3/15(日)"
);

if (sf1) {
    drawTeamWithFlagCentered(
    SF_LEFT_X + BOX_W / 2,
    Y_SF + 45,
    buildTeamText(sf1.homeTeam, sf1.homeScore)
    );

    drawTeamWithFlagCentered(
    SF_LEFT_X + BOX_W / 2,
    Y_SF + 80,
    buildTeamText(sf1.awayTeam, sf1.awayScore)
    );
}

if (sf1?.winnerTeam) {
  drawWinnerWithFlagCentered(
    SF_LEFT_X + BOX_W / 2,
    Y_SF + 110,
    `Win：${sf1.winnerTeam}`
  );
}

// ---------- 準決勝② ----------
const sf2 = tournament.SF?.[2];

drawBox(
  ctx,
  SF_RIGHT_X,
  Y_SF,
  BOX_W,
  BOX_H,
  "",
  [
    "",
    "vs",
    "",
    null
  ]
);

BOX_HIT_AREAS.push({
  x: SF_RIGHT_X,
  y: Y_SF,
  w: BOX_W,
  h: BOX_H,
  title: "② 準決勝",
  data: sf2
});

drawRoundTitleCentered(
  SF_RIGHT_X + BOX_W / 2,
  Y_SF + 18,
  "② 準決勝 '26/3/16(月)"
);

if (sf2) {
drawTeamWithFlagCentered(
  SF_RIGHT_X + BOX_W / 2,
  Y_SF + 45,
  buildTeamText(sf2.homeTeam, sf2.homeScore)
);

drawTeamWithFlagCentered(
  SF_RIGHT_X + BOX_W / 2,
  Y_SF + 80,
  buildTeamText(sf2.awayTeam, sf2.awayScore)
);
}

if (sf2?.winnerTeam) {
  drawWinnerWithFlagCentered(
    SF_RIGHT_X + BOX_W / 2,
    Y_SF + 110,
    `Win：${sf2.winnerTeam}`
  );
}

    vLine(CENTER_X, Y_FINAL + BOX_H, Y_FINAL + BOX_H + CONNECT);
    hLine(SF_LEFT_X + BOX_W / 2, SF_RIGHT_X + BOX_W / 2, Y_FINAL + BOX_H + CONNECT);
    vLine(SF_LEFT_X + BOX_W / 2,  Y_FINAL + BOX_H + CONNECT, Y_SF);
    vLine(SF_RIGHT_X + BOX_W / 2, Y_FINAL + BOX_H + CONNECT, Y_SF);

    // 準々決勝
    const QF_L1_X = SF_LEFT_X  - BOX_W / 2;
    const QF_L2_X = SF_LEFT_X  + BOX_W / 2;
    const QF_R1_X = SF_RIGHT_X - BOX_W / 2;
    const QF_R2_X = SF_RIGHT_X + BOX_W / 2;

    

// ==============================
// 準々決勝（QF①〜④ 国旗付き・中央揃え統一）
// ==============================

// ---------- QF① ----------
const qf1 = tournament.QF?.[1];
drawBox(
  ctx,
  QF_L1_X,
  Y_QF,
  BOX_W,
  BOX_H,
  "",
  [
    "",
    "vs",
    "",
    null
  ]
);


BOX_HIT_AREAS.push({
  x: QF_L1_X,
  y: Y_QF,
  w: BOX_W,
  h: BOX_H,
  title: "① 準々決勝",
  data: qf1
});

drawRoundTitleCentered(
  QF_L1_X + BOX_W / 2,
  Y_QF + 18,
  "① 準々決勝 '26/3/13(金)"
);
if (qf1) {
  drawTeamWithFlagCentered(
    QF_L1_X + BOX_W / 2,
    Y_QF + 45,
    buildTeamText(qf1.homeTeam, qf1.homeScore)
  );
  drawTeamWithFlagCentered(
    QF_L1_X + BOX_W / 2,
    Y_QF + 81,
    buildTeamText(qf1.awayTeam, qf1.awayScore)  );
}
if (qf1?.winnerTeam) {
  drawWinnerWithFlagCentered(
    QF_L1_X + BOX_W / 2,
    Y_QF + 111,
    `Win：${qf1.winnerTeam}`
  );
}

// ---------- QF② ----------
const qf2 = tournament.QF?.[2];
drawBox(
  ctx,
  QF_L2_X,
  Y_QF,
  BOX_W,
  BOX_H,
  "",
  [
    "",
    "vs",
    "",
    null
  ]
);

BOX_HIT_AREAS.push({
  x: QF_L2_X,
  y: Y_QF,
  w: BOX_W,
  h: BOX_H,
  title: "② 準々決勝",
  data: qf2
});

drawRoundTitleCentered(
  QF_L2_X + BOX_W / 2,
  Y_QF + 18,
  "② 準々決勝 '26/3/14(土)"
);
if (qf2) {
  drawTeamWithFlagCentered(
    QF_L2_X + BOX_W / 2,
    Y_QF + 45,
    buildTeamText(qf2.homeTeam, qf2.homeScore)
  );
  drawTeamWithFlagCentered(
    QF_L2_X + BOX_W / 2,
    Y_QF + 81,
    buildTeamText(qf2.awayTeam, qf2.awayScore)  );
}
if (qf2?.winnerTeam) {
  drawWinnerWithFlagCentered(
    QF_L2_X + BOX_W / 2,
    Y_QF + 111,
    `Win：${qf2.winnerTeam}`
  );
}

// ---------- QF③ ----------
const qf3 = tournament.QF?.[3];
drawBox(
  ctx,
  QF_R1_X,
  Y_QF,
  BOX_W,
  BOX_H,
  "",
  [
    "",
    "vs",
    "",
    null
  ]
);

BOX_HIT_AREAS.push({
  x: QF_R1_X,
  y: Y_QF,
  w: BOX_W,
  h: BOX_H,
  title: "③ 準々決勝",
  data: qf3
});

drawRoundTitleCentered(
  QF_R1_X + BOX_W / 2,
  Y_QF + 18,
  "③ 準々決勝 '26/3/13(金)"
);
if (qf3) {
  drawTeamWithFlagCentered(
    QF_R1_X + BOX_W / 2,
    Y_QF + 45,
    buildTeamText(qf3.homeTeam, qf3.homeScore)
  );
  drawTeamWithFlagCentered(
    QF_R1_X + BOX_W / 2,
    Y_QF + 81,
    buildTeamText(qf3.awayTeam, qf3.awayScore)  );
}
if (qf3?.winnerTeam) {
  drawWinnerWithFlagCentered(
    QF_R1_X + BOX_W / 2,
    Y_QF + 111,
    `Win：${qf3.winnerTeam}`
  );
}

// ---------- QF④ ----------
const qf4 = tournament.QF?.[4];
drawBox(
  ctx,
  QF_R2_X,
  Y_QF,
  BOX_W,
  BOX_H,
  "",
  [
    "",
    "vs",
    "",
    null
  ]
);

BOX_HIT_AREAS.push({
  x: QF_R2_X,
  y: Y_QF,
  w: BOX_W,
  h: BOX_H,
  title: "④ 準々決勝",
  data: qf4
});

drawRoundTitleCentered(
  QF_R2_X + BOX_W / 2,
  Y_QF + 18,
  "④ 準々決勝 '26/3/14(土)"
);
if (qf4) {
  drawTeamWithFlagCentered(
    QF_R2_X + BOX_W / 2,
    Y_QF + 45,
    buildTeamText(qf4.homeTeam, qf4.homeScore)
  );
  drawTeamWithFlagCentered(
    QF_R2_X + BOX_W / 2,
    Y_QF + 81,
    buildTeamText(qf4.awayTeam, qf4.awayScore));
}
if (qf4?.winnerTeam) {
  drawWinnerWithFlagCentered(
    QF_R2_X + BOX_W / 2,
    Y_QF + 111,
    `Win：${qf4.winnerTeam}`
  );
}


    // 接続線
    const QF_LEFT_CENTER_1 = QF_L1_X + BOX_W / 2;
    const QF_LEFT_CENTER_2 = QF_L2_X + BOX_W / 2;
    const SF_LEFT_CENTER  = SF_LEFT_X + BOX_W / 2;

    vLine(QF_LEFT_CENTER_1, Y_QF, Y_QF - CONNECT);
    vLine(QF_LEFT_CENTER_2, Y_QF, Y_QF - CONNECT);
    hLine(QF_LEFT_CENTER_1, QF_LEFT_CENTER_2, Y_QF - CONNECT);
    vLine(SF_LEFT_CENTER, Y_QF - CONNECT, Y_SF + BOX_H);
    hLine(QF_LEFT_CENTER_1, SF_LEFT_CENTER, Y_QF - CONNECT);

    const QF_RIGHT_CENTER_1 = QF_R1_X + BOX_W / 2;
    const QF_RIGHT_CENTER_2 = QF_R2_X + BOX_W / 2;
    const SF_RIGHT_CENTER  = SF_RIGHT_X + BOX_W / 2;

    vLine(QF_RIGHT_CENTER_1, Y_QF, Y_QF - CONNECT);
    vLine(QF_RIGHT_CENTER_2, Y_QF, Y_QF - CONNECT);
    hLine(QF_RIGHT_CENTER_1, QF_RIGHT_CENTER_2, Y_QF - CONNECT);
    vLine(SF_RIGHT_CENTER, Y_QF - CONNECT, Y_SF + BOX_H);
    hLine(QF_RIGHT_CENTER_2, SF_RIGHT_CENTER, Y_QF - CONNECT);
  }

  // 初回描画
  redraw();
});
