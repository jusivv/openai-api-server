export default {
    moduleCache: {
        vue: Vue
    },
    async getFile(url) {
        const res = await fetch(url);
        if (!res.ok) {
            throw Object.assign(new Error(res.statusText + ' ' + url), {res});
        }
        if (url.endsWith(".js")) {
            return {
                getContentData: asBinary => asBinary ? res.arrayBuffer() : res.text(),
                type: ".mjs"
            }
        } else {
            return {
                getContentData: asBinary => asBinary ? res.arrayBuffer() : res.text(),
            }
        }
    },
    addStyle(textContent) {
        const style = Object.assign(document.createElement('style'), {textContent});
        const ref = document.head.getElementsByTagName('style')[0] || null;
        document.head.insertBefore(style, ref);
    }
}