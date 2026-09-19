let protectedUrl = null;
let proxyId = null;
let refillRate = 0;
let lastTokens = 0;

async function generateAPI() {

    const url = document.getElementById("url").value.trim();
    const capacity = Number(document.getElementById("capacity").value);
    refillRate = Number(document.getElementById("refillRate").value);

    if (!url || capacity < 1 || refillRate < 0) {
        alert("Enter valid API configuration.");
        return;
    }

    const res = await fetch("/api/protect", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({
            url,
            capacity,
            refillRate
        })
    });

    if (!res.ok) {
        alert(await res.text());
        return;
    }

    const data = await res.json();

    protectedUrl = data.protectedUrl;
    proxyId = protectedUrl.split("/").pop();

    document.getElementById("protectedUrl").textContent =
        protectedUrl;

    document.getElementById("generated")
        .classList.remove("hidden");

    document.getElementById("checkBtn").disabled = false;

    document.getElementById("refillDisplay").textContent =
        refillRate + " / sec";

    updateStatus();
}


async function checkAPI() {

    if (!protectedUrl) return;

    const start = performance.now();

    const res = await fetch(protectedUrl);

    const latency =
        Math.round(
            performance.now() - start
        );

    document.getElementById("latency").textContent =
        latency + " ms";

    const box =
        document.getElementById("response");

    if (res.status === 429) {

        box.textContent =
            "✕ Rate limit exceeded • HTTP 429";

        box.style.color =
            "#ff5577";

    } else if (res.ok) {

        box.textContent =
            "✓ Request allowed • HTTP " +
            res.status;

        box.style.color =
            "#35e6a1";

    } else {

        box.textContent =
            "HTTP " + res.status;

        box.style.color =
            "#8992a7";
    }

    updateStatus();
}


async function updateStatus() {

    if (!proxyId) return;

    const res =
        await fetch(
            "/proxy/" +
            proxyId +
            "/status"
        );

    if (!res.ok) return;

    const data =
        await res.json();

    lastTokens =
        data.tokens;

    document.getElementById("tokens")
        .textContent =
        Math.floor(data.tokens);

    document.getElementById("capacityDisplay")
        .textContent =
        data.capacity;

    document.getElementById("tokenBar")
        .style.width =
        Math.min(
            100,
            data.tokens /
            data.capacity *
            100
        ) + "%";

    document.getElementById("statusDot")
        .style.background =
        data.tokens >= 1
            ? "#35e6a1"
            : "#ff5577";

    if (
        data.tokens < data.capacity &&
        refillRate > 0
    ) {

        document.getElementById("nextRefill")
            .textContent =
            "~" +
            (1 / refillRate)
                .toFixed(1) +
            " sec";

    } else {

        document.getElementById("nextRefill")
            .textContent =
            "Bucket full";
    }
}


function copyURL() {

    navigator.clipboard
        .writeText(protectedUrl);

    document.getElementById("copyMessage")
        .textContent =
        "Protected URL copied";

    setTimeout(() => {

        document.getElementById("copyMessage")
            .textContent = "";

    }, 1800);
}


setInterval(
    updateStatus,
    1000
);