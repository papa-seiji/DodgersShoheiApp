document.addEventListener("DOMContentLoaded", async () => {
  const canvas = document.getElementById("wbc-tournament-canvas");
  if (!canvas) return;

// ===== 拡大表示用 Canvas =====
const previewCanvas = document.getElementById("box-preview-canvas");
const previewCtx = previewCanvas.getContext("2d");

const ctx = canvas.getContext("2d"); // ★ 必須



// ==============================
// フォント基準サイズ
// ==============================
const FONT_TITLE_BASE  = 14;
const FONT_TEXT_BASE   = 12;
const FONT_WIN_BASE    = 12;
const FONT_CHAMP_BASE  = 16;



  // ==============================
  // DBデータ取得
  // ==============================
  const res = await fetch("/api/wbc/tournament?year=2026");
  const matches = await res.json();

  let tournament = {};
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
    "CZECH REPUBLIC": "https://dodgersshoheiapp-assets.s3.us-east-1.amazonaws.com/img/2026_WBC/flags/cz.jpg",
    "CHINESE TAIPEI": "https://dodgersshoheiapp-assets.s3.us-east-1.amazonaws.com/img/2026_WBC/flags/tw.jpg",
    "UNITED KINGDOM": "https://dodgersshoheiapp-assets.s3.us-east-1.amazonaws.com/img/2026_WBC/flags/gb.jpg"
  };

  const images = {};
  Object.keys(srcMap).forEach(k => {
    const img = new Image();
img.onload = () => {
  if (window.redrawTournament) {
    window.redrawTournament();
  }
};
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
  ctx.font = `bold ${FONT_MD}px sans-serif`;

  ctx.fillStyle = "#000";
  ctx.textAlign = "center";
  ctx.textBaseline = "middle";
  ctx.fillText(title, x + w / 2, y + 18);

  // ④ 中身テキスト（★プレビュー時は小さく）
  const fontLg = ctx.__PREVIEW_FONT_LG || FONT_LG;
  const fontMd = ctx.__PREVIEW_FONT_MD || FONT_MD;

  lines.forEach((t, i) => {
    if (t != null) {

      // vs / Win 行は少し小さく
      if (t === "vs" || t.startsWith("Win")) {
        ctx.font = `bold ${fontMd}px sans-serif`;
      } else {
        ctx.font = `bold ${fontLg}px sans-serif`;
      }

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

  // ★ 表示用は日本語に変換
  const displayTeam = toJapaneseTeamName(team);

  // スコア未入力（null / undefined）の場合は表示しない
  if (score === null || score === undefined) {
    return displayTeam;
  }

  // 0 も含めて表示する
  return `${displayTeam} ${score}`;
}



//これは Canvas 本体ではなく、モーダル（拡大表示）専用のロジックです。
function buildPreviewLines(data) {
  if (!data) return ["未定"];

  const lines = [];

  const hasHome = !!data.homeTeam;
  const hasAway = !!data.awayTeam;

  if (hasHome) {
    lines.push(buildTeamText(data.homeTeam, data.homeScore));
  }

  // ★ 両チームがある場合のみ vs
  if (hasHome && hasAway) {
    lines.push("vs");
  }

  if (hasAway) {
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

  ctx.font = `bold ${FONT_LG}px sans-serif`;
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
// 国旗＋チーム名（中央揃え・英語キー分離版）
// ==============================
function drawTeamWithFlagCentered(cx, y, teamCode, displayText) {

  if (!teamCode || !displayText) return;
console.log("FLAG TEAM =", teamCode);
  ctx.font = `bold ${FONT_LG}px sans-serif`;
  ctx.textAlign = "left";

  const flag = FLAGS[teamCode];

  const FLAG_W = flag ? 26 : 0;
  const FLAG_H = 18;
  const GAP = flag ? 6 : 0;

  const textWidth = ctx.measureText(displayText).width;
  const totalWidth = FLAG_W + GAP + textWidth;

  const startX = cx - totalWidth / 2;

  // 国旗
  if (flag && flag.complete && flag.naturalWidth > 0) {
    ctx.drawImage(flag, startX, y - FLAG_H / 2, FLAG_W, FLAG_H);
  }

  // テキスト
  ctx.fillText(displayText, startX + FLAG_W + GAP, y);
}


// ==============================
// ラウンドタイトル（中央寄せ）
// ==============================
function drawRoundTitleCentered(cx, y, text) {
  if (!text) return;

  ctx.font = `bold ${FONT_MD}px sans-serif`;

  ctx.textAlign = "center";
  ctx.fillText(text, cx, y);
}


// ==============================
// 勝者（国旗＋中央揃え）
// ==============================
// function drawWinnerWithFlagCentered(cx, y, winnerTeam) {
//   if (!winnerTeam) return;
//   drawTeamWithFlagCentered(cx, y, winnerTeam);
// }

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
// ★ 全体表示スケール（ここだけ触る）
// ==============================
const UI_SCALE = 1.3; // ← 1.2 / 1.4 / 1.5 で全体サイズ調整

// ==============================
// レイアウト（★UI_SCALE 完全対応版）
// ==============================

// X 方向
// const CENTER_X = 550 * UI_SCALE;
const CENTER_X = canvas.width / 2;

const GAP_X    = 213 * UI_SCALE;

// BOX サイズ
const BOX_W_BASE = 210;
const BOX_H_BASE = 135;

const BOX_W = BOX_W_BASE * UI_SCALE;
const BOX_H = BOX_H_BASE * UI_SCALE;

// Y 方向（★BASE → SCALE）
const TOP_MARGIN_BASE = 60;

const Y_CHAMP_BASE = TOP_MARGIN_BASE + 60;
const Y_FINAL_BASE = TOP_MARGIN_BASE + 200;
const Y_SF_BASE    = TOP_MARGIN_BASE + 400;
const Y_QF_BASE    = TOP_MARGIN_BASE + 590;

const Y_CHAMP = Y_CHAMP_BASE * UI_SCALE;
const Y_FINAL = Y_FINAL_BASE * UI_SCALE;
const Y_SF    = Y_SF_BASE    * UI_SCALE;
const Y_QF    = Y_QF_BASE    * UI_SCALE;

// 接続線
const CONNECT = 30 * UI_SCALE;

// ==============================
// フォントサイズ（等倍対応）
// ==============================
const FONT_SM = 12 * UI_SCALE;
const FONT_MD = 14 * UI_SCALE;
const FONT_LG = 19 * UI_SCALE;


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
  const scale = 1.0;

  const w = BOX_W * scale;
  const h = BOX_H * scale;

  ctx.clearRect(0, 0, canvas.width, canvas.height);

  ctx.save(); // ★ ここ重要

  // ★ プレビュー用にフォントを小さく
  const PREVIEW_FONT_LG = FONT_LG * 0.75;
  const PREVIEW_FONT_MD = FONT_MD * 0.75;

  // 一時的に差し替え
  ctx.__PREVIEW_FONT_LG = PREVIEW_FONT_LG;
  ctx.__PREVIEW_FONT_MD = PREVIEW_FONT_MD;

  drawBox(
    ctx,
    (canvas.width - w) / 2,
    (canvas.height - h) / 2,
    w,
    h,
    hit.title,
    buildPreviewLines(hit.data)
  );

  ctx.restore(); // ★ 戻す
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

window.redrawTournament = async function redrawTournament() {

  const res = await fetch("/api/wbc/tournament?year=2026");
  const matches = await res.json();

  tournament = {};
  matches.forEach(m => {
    if (!tournament[m.round]) tournament[m.round] = {};
    tournament[m.round][m.matchNo] = m;
  });


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

  ctx.font = `bold ${FONT_LG}px sans-serif`; // ← 太字＋少し大きく
drawChampionWithFlagCentered(
  CENTER_X,
  Y_CHAMP + 90,
  champion
);

  ctx.restore();
} else {
  ctx.font = `bold ${FONT_LG}px sans-serif`;
  ctx.textAlign = "center";
  ctx.fillText("未定", CENTER_X, Y_CHAMP + 70);
}

// ==============================
// 決勝（FINAL）※ 準決勝と同一思想
// ==============================
const final = tournament.FINAL?.[1];
const hasFinalTeams = final?.homeTeam || final?.awayTeam;

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
    "",
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
  Y_FINAL + 20,
  "決勝 '26/3/17(火)"
);


if (hasFinalTeams) {

  // 上チーム
  drawTeamWithFlagCentered(
    CENTER_X,
    Y_FINAL + 60,
    final.homeTeam,
    buildTeamText(final.homeTeam, final.homeScore)
  );

  // vs
  ctx.save();
  ctx.font = `bold ${FONT_MD}px sans-serif`;
  ctx.textAlign = "center";
  ctx.fillText("vs", CENTER_X, Y_FINAL + 85);
  ctx.restore();

  // 下チーム
  drawTeamWithFlagCentered(
    CENTER_X,
    Y_FINAL + 110,
    final.awayTeam,
    buildTeamText(final.awayTeam, final.awayScore)
  );

  // 勝者
  if (final.winnerTeam) {
    drawWinnerWithFlagCentered(
      CENTER_X,
      Y_FINAL + 145,
      `Win：${final.winnerTeam}`
    );
  }

} else {
  // ★ 完全未定時
  ctx.save();
  ctx.font = `bold ${FONT_LG}px sans-serif`;
  ctx.textAlign = "center";
  ctx.fillText("未定", CENTER_X, Y_FINAL + 90);
  ctx.restore();
}

// 上位（優勝）との接続線は既存を維持
vLine(CENTER_X, Y_CHAMP + BOX_H, Y_FINAL);



    // 準決勝
    const SF_LEFT_X  = CENTER_X - GAP_X - BOX_W / 2;
    const SF_RIGHT_X = CENTER_X + GAP_X - BOX_W / 2;

// ---------- 準決勝① ----------
const sf1 = tournament.SF?.[1];
const hasSf1Teams = sf1?.homeTeam || sf1?.awayTeam;

// 枠
drawBox(
  ctx,
  SF_LEFT_X,
  Y_SF,
  BOX_W,
  BOX_H,
  "",
  ["", "", "", null]
);

BOX_HIT_AREAS.push({
  x: SF_LEFT_X,
  y: Y_SF,
  w: BOX_W,
  h: BOX_H,
  title: "① 準決勝",
  data: sf1
});

// タイトル
drawRoundTitleCentered(
  SF_LEFT_X + BOX_W / 2,
  Y_SF + 20,
  "① 準決勝 '26/3/15(日)"
);

if (hasSf1Teams) {

  // 上チーム（存在する場合のみ）
  if (sf1.homeTeam) {
    drawTeamWithFlagCentered(
      SF_LEFT_X + BOX_W / 2,
      Y_SF + 60,
      sf1.homeTeam,
      buildTeamText(sf1.homeTeam, sf1.homeScore)
    );
  }

  // vs（両チームある時のみ）
  if (sf1.homeTeam && sf1.awayTeam) {
    ctx.save();
    ctx.font = `bold ${FONT_MD}px sans-serif`;
    ctx.textAlign = "center";
    ctx.fillText("vs", SF_LEFT_X + BOX_W / 2, Y_SF + 85);
    ctx.restore();
  }

  // 下チーム
  if (sf1.awayTeam) {
    drawTeamWithFlagCentered(
      SF_LEFT_X + BOX_W / 2,
      Y_SF + 110,
      sf1.awayTeam,
      buildTeamText(sf1.awayTeam, sf1.awayScore)
    );
  }

  // 勝者
  if (sf1.winnerTeam) {
    drawWinnerWithFlagCentered(
      SF_LEFT_X + BOX_W / 2,
      Y_SF + 145,
      `Win：${sf1.winnerTeam}`
    );
  }

} else {
  // ★ 完全未定
  ctx.save();
  ctx.font = `bold ${FONT_LG}px sans-serif`;
  ctx.textAlign = "center";
  ctx.fillText("未定", SF_LEFT_X + BOX_W / 2, Y_SF + 90);
  ctx.restore();
}

// ---------- 準決勝② ----------
const sf2 = tournament.SF?.[2];
const hasSf2Teams = sf2?.homeTeam || sf2?.awayTeam;

// 枠
drawBox(
  ctx,
  SF_RIGHT_X,
  Y_SF,
  BOX_W,
  BOX_H,
  "",
  ["", "", "", null]
);

BOX_HIT_AREAS.push({
  x: SF_RIGHT_X,
  y: Y_SF,
  w: BOX_W,
  h: BOX_H,
  title: "② 準決勝",
  data: sf2
});

// タイトル
drawRoundTitleCentered(
  SF_RIGHT_X + BOX_W / 2,
  Y_SF + 20,
  "② 準決勝 '26/3/16(月)"
);

if (hasSf2Teams) {

  // 上チーム
  if (sf2.homeTeam) {
    drawTeamWithFlagCentered(
      SF_RIGHT_X + BOX_W / 2,
      Y_SF + 60,
      sf2.homeTeam,
      buildTeamText(sf2.homeTeam, sf2.homeScore)
    );
  }

  // vs（両チームある場合のみ）
  if (sf2.homeTeam && sf2.awayTeam) {
    ctx.save();
    ctx.font = `bold ${FONT_MD}px sans-serif`;
    ctx.textAlign = "center";
    ctx.fillText(
      "vs",
      SF_RIGHT_X + BOX_W / 2,
      Y_SF + 85
    );
    ctx.restore();
  }

  // 下チーム
  if (sf2.awayTeam) {
    drawTeamWithFlagCentered(
      SF_RIGHT_X + BOX_W / 2,
      Y_SF + 110,
      sf2.awayTeam,
      buildTeamText(sf2.awayTeam, sf2.awayScore)
    );
  }

  // 勝者
  if (sf2.winnerTeam) {
    drawWinnerWithFlagCentered(
      SF_RIGHT_X + BOX_W / 2,
      Y_SF + 145,
      `Win：${sf2.winnerTeam}`
    );
  }

} else {
  // ★ 完全未定
  ctx.save();
  ctx.font = `bold ${FONT_LG}px sans-serif`;
  ctx.textAlign = "center";
  ctx.fillText(
    "未定",
    SF_RIGHT_X + BOX_W / 2,
    Y_SF + 90
  );
  ctx.restore();
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


// 準々決勝 固定表示文言（B案）
const QF_FIXED_LABELS = {
  1: ["POOL A 2位", "POOL B 1位"],
  2: ["POOL B 2位", "POOL A 1位"],
  3: ["POOL C 2位", "POOL D 1位"],
  4: ["POOL D 2位", "POOL C 1位"]
};


function drawQuarterFinal({
  ctx,
  x,
  y,
  boxW,
  boxH,
  title,
  match,
  hitTitle
}) {
  // 枠は必ず描く
  drawBox(
    ctx,
    x,
    y,
    boxW,
    boxH,
    "",
    ["", "", "", null]
  );

  BOX_HIT_AREAS.push({
    x,
    y,
    w: boxW,
    h: boxH,
    title: hitTitle,
    data: match
  });

  // タイトル
  drawRoundTitleCentered(
    x + boxW / 2,
    y + 20,
    title
  );

  // ★★★ B案：ここに入れる ★★★
  if (!match?.homeTeam && !match?.awayTeam) {
    const labels = QF_FIXED_LABELS[match.matchNo];

    ctx.save();
    ctx.font = `bold ${FONT_LG}px sans-serif`;
    ctx.textAlign = "center";

    ctx.fillText(labels[0], x + boxW / 2, y + 70);
    ctx.fillText("vs",        x + boxW / 2, y + 90);
    ctx.fillText(labels[1], x + boxW / 2, y + 110);

    ctx.restore();
    return; // ← この試合の描画だけ終了
  }
  // ★★★ ここまで ★★★

  const hasTeams = match?.homeTeam || match?.awayTeam;

  if (hasTeams) {
    if (match.homeTeam) {
      drawTeamWithFlagCentered(
        x + boxW / 2,
        y + 60,
        match.homeTeam,
        buildTeamText(match.homeTeam, match.homeScore)
      );
    }

    if (match.homeTeam && match.awayTeam) {
      ctx.save();
      ctx.font = `bold ${FONT_MD}px sans-serif`;
      ctx.textAlign = "center";
      ctx.fillText("vs", x + boxW / 2, y + 85);
      ctx.restore();
    }

    if (match.awayTeam) {
      drawTeamWithFlagCentered(
        x + boxW / 2,
        y + 110,
        match.awayTeam,
        buildTeamText(match.awayTeam, match.awayScore)
      );
    }
  } else {
    ctx.save();
    ctx.font = `bold ${FONT_LG}px sans-serif`;
    ctx.textAlign = "center";
    ctx.fillText("未定", x + boxW / 2, y + 90);
    ctx.restore();
  }
}

drawQuarterFinal({
  ctx,
  x: QF_L1_X,
  y: Y_QF,
  boxW: BOX_W,
  boxH: BOX_H,
  title: "① 準々決勝 '26/3/13(金)",
  match: tournament.QF?.[1],
  hitTitle: "① 準々決勝"
});

drawQuarterFinal({
  ctx,
  x: QF_L2_X,
  y: Y_QF,
  boxW: BOX_W,
  boxH: BOX_H,
  title: "② 準々決勝 '26/3/14(土)",
  match: tournament.QF?.[2],
  hitTitle: "② 準々決勝"
});

drawQuarterFinal({
  ctx,
  x: QF_R1_X,
  y: Y_QF,
  boxW: BOX_W,
  boxH: BOX_H,
  title: "③ 準々決勝 '26/3/13(金)",
  match: tournament.QF?.[3],
  hitTitle: "③ 準々決勝"
});

drawQuarterFinal({
  ctx,
  x: QF_R2_X,
  y: Y_QF,
  boxW: BOX_W,
  boxH: BOX_H,
  title: "④ 準々決勝 '26/3/14(土)",
  match: tournament.QF?.[4],
  hitTitle: "④ 準々決勝"
});


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


// ==============================
// チーム名 英語 → 日本語（表示用）
// ==============================
const TEAM_NAME_JA_MAP = {
  "PUERTO RICO": "プエルトリコ",
  "CUBA": "キューバ",
  "CANADA": "カナダ",
  "PANAMA": "パナマ",
  "COLOMBIA": "コロンビア",
  "USA": "アメリカ",
  "MEXICO": "メキシコ",
  "ITALY": "イタリア",
  "UNITED KINGDOM": "イギリス",
  "BRAZIL": "ブラジル",
  "JAPAN": "日本",
  "AUSTRALIA": "オーストラリア",
  "KOREA": "韓国",
  "CZECH REPUBLIC": "チェコ",
  "CHINESE TAIPEI": "タイペイ",
  "VENEZUELA": "ベネズエラ",
  "DOMINICAN REPUBLIC": "ドミニカ",
  "NETHERLANDS": "オランダ",
  "ISRAEL": "イスラエル",
  "NICARAGUA": "ニカラグア"
};

function toJapaneseTeamName(teamName) {
  if (!teamName) return "";
  return TEAM_NAME_JA_MAP[teamName] || teamName;
}


function drawChampionWithFlagCentered(cx, y, teamName) {
  if (!teamName) return;

  ctx.save();
  ctx.font = `bold ${FONT_LG}px sans-serif`;
  ctx.textAlign = "left";

  const label = "Win : ";

  const teamKey   = teamName;                     // 🇯🇵 国旗キー
  const teamLabel = toJapaneseTeamName(teamName); // 日本 表示

  const flag = FLAGS[teamKey];

  const FLAG_W = flag ? 30 : 0;
  const FLAG_H = 20;
  const GAP = flag ? 8 : 0;

  const labelWidth = ctx.measureText(label).width;
  const teamWidth  = ctx.measureText(teamLabel).width;

  const totalWidth = labelWidth + FLAG_W + GAP + teamWidth;
  const startX = cx - totalWidth / 2;

  // Win :
  ctx.fillText(label, startX, y);

  // 国旗
  if (flag && flag.complete && flag.naturalWidth > 0) {
    ctx.drawImage(flag, startX + labelWidth, y - FLAG_H / 2, FLAG_W, FLAG_H);
  }

  // 日本語チーム名
  ctx.fillText(
    teamLabel,
    startX + labelWidth + FLAG_W + GAP,
    y
  );

  ctx.restore();
}


  // 初回描画
  redrawTournament();
});
