<template>
    <div id="root">
        <a-layout style="min-height: 100%">
            <a-layout-header>
                <p class="txt-title">ChatGPT</p>
            </a-layout-header>
            <a-layout>
                <a-layout-sider breakpoint="md" collapsed-width="0" theme="light">
                    <a-layout style="min-height: 100%">
                        <a-layout-content style="padding: 5px">
                            <a-space direction="vertical" :style="{width: '100%'}">
                                <a-button v-for="(c, i) in conversationList"
                                          :type="c.contextId === conversationId ? 'primary' : ''"
                                          block
                                          @click="switchConversation(c.contextId)">
                                    {{c.contextTitle || "New conversation"}}
                                </a-button>
                            </a-space>
                        </a-layout-content>
                        <a-layout-footer :style="{padding: '10px'}">
                            <a-space direction="vertical" :style="{width: '100%'}">
                                <a-button type="primary" block ghost @click="newChat">New chat</a-button>
                                <a-button type="primary" block ghost danger>Clear all</a-button>
                                <a-space>
                                    <a-switch v-model:checked="showMarkdown" checked-children="M" un-checked-children="H"></a-switch>
                                    <a-switch v-model:checked="useStream" checked-children="S" un-checked-children="N"></a-switch>
                                </a-space>
                            </a-space>
                        </a-layout-footer>
                    </a-layout>
                </a-layout-sider>
                <a-layout>
                    <a-layout-content ref="chat-layout" :style="{overflow: 'auto', padding: '10px', height: 'calc(100vh - 170px)'}">
                        <div ref="chat-content" :hidden="showMarkdown">
                            <div v-for="message in messages">
                                <div v-if="message.isAnswer" class="speaker">
                                    <a-avatar style="color: #f56a00; background-color: #fde3cf">
                                        GPT
                                    </a-avatar>
                                    ({{ message.time || now() }})
                                </div>
                                <div v-else  class="speaker">
                                    <a-avatar style="background-color: #87d068">
                                        Me
                                    </a-avatar>
                                    ({{ message.time || now() }})
                                </div>
<!--                                <div class="speaker">{{message.isAnswer ? 'ChatGPT:' : 'Me:'}}&nbsp;({{now()}})</div>-->
                                <div v-if="message.isAnswer" style="padding-left: 40px" v-html="parseHtml(message.content)">
                                </div>
                                <div v-else  style="padding-left: 40px">{{message.content}}</div>
                                <br/>
                            </div>
                            <div v-if="!useStream && working">
                                <a-spin />
                            </div>
                        </div>
                        <div :hidden="!showMarkdown" style="height: 100%">
                            <textarea style="height: 100%; width: 100%">{{mdContent}}</textarea>
                        </div>
                    </a-layout-content>
                    <a-layout-footer :style="{padding: '10px'}">
                        <a-input-group compact>
                            <a-textarea v-model:value="question" placeholder="Let's talk about ..." style="width: calc(100% - 140px); height: 60px" :disabled="working"></a-textarea>
                            <a-button type="primary" style="height: 60px" :disabled="!canSend" @click="send">Send</a-button>
                            <a-popconfirm
                                    title="Delete this conversation ?"
                                    ok-text="Yes"
                                    cancel-text="No"
                                    @confirm="deleteConversation"
                                    :disabled="conversationId === ''"
                            >
                                <a-button type="primary" ghost style="height: 60px" :disabled="working">Delete</a-button>
                            </a-popconfirm>
                        </a-input-group>
                    </a-layout-footer>
                </a-layout>
            </a-layout>
        </a-layout>
    </div>
</template>
<script type="module">
const { createApp } = Vue
import { post, get } from "/app/request.js"
import { marked } from "/md/marked.esm.js"
const prompt = "你好！我的AI助手"
export default {
    name: "Chat",
    data() {
        return {
            question: "",
            conversationId: "",
            messages: [],
            totalTokens: 0,
            conversationList: [],
            working: false,
            lastContentHeight: 0,
            showMarkdown: false,
            useStream: true,
        }
    },
    computed: {
        canSend() {
            const { conversationId, question, working } = this
            return question.trim().length > 0 && !working && conversationId
        },
        mdContent() {
            const { messages } = this
            let md = "# ChatGPT"
            if (messages) {
                messages.forEach(m => {
                    md += "\n\n" + (!m.isAnswer ? "## " : "") + m.content
                })
            }
            return md
        }
    },
    updated() {
        // this.flushContent()
    },
    mounted() {
        if (!sessionStorage.sessionId && !localStorage.token) {
            this.$router.push("/login")
        }
        new MutationObserver(() => {
            this.flushContent()
        }).observe(this.$refs["chat-content"], {
            childList: true
        })
        this.listConversation()
    },
    methods: {
        listConversation() {
            get("/context/list")
                .then(data => this.conversationList = data)
                .catch(err => console.log("get context list failed, %s", err))
        },
        getContentHeight() {
            let div = this.$refs["chat-content"]
            if (div) {
                return div.scrollHeight
            } else {
                return 0
            }
        },
        flushContent() {
            hljs.highlightAll()
            let layout = this.$refs["chat-layout"]
            let content = this.$refs["chat-content"]
            if (layout.offsetHeight < content.scrollHeight) {
                layout.scrollTo({ top: this.lastContentHeight, behavior: 'smooth' });
            }
        },
        parseHtml(src) {
            return marked.parse(src)
        },
        newChat() {
            const { clear } = this
            clear()
            const { message } = antd
            post("/context/create", {
                question: prompt
            }).then(data => {
                this.conversationList.push(data)
                message.success("OK! Let's start talking")
            }).catch(err => alert(err))
        },
        send() {
            const { question, conversationId, useStream, ask, askWithSse } = this
            if (question && conversationId) {
                if (useStream) {
                    askWithSse(question, conversationId)
                } else {
                    ask(question, conversationId)
                }
            }
        },
        updateConversationTitle(conversationId) {
            const { getConversation } = this
            let conversation = getConversation(conversationId)
            if (conversation && !conversation.contextTitle) {
                get("/chat/getTitle/" + conversationId)
                    .then(data => conversation.contextTitle = data.answer)
                    .catch(err => alert(err))
            }
        },
        getConversation(conversationId) {
            const { conversationList } = this
            return conversationList.find(c => c.contextId === conversationId)
        },
        clear() {
            this.conversationId = ""
            this.messages = []
            this.question = ""
            this.totalTokens = 0
        },
        ask(content, conversationId) {
            this.useLoadingState = true
            if (content && conversationId) {
                // record Y
                this.lastContentHeight = this.getContentHeight()
                // push question
                this.messages.push({
                    isAnswer: false,
                    content
                })
                this.working = true
                post("/chat/ask", {
                    question: content,
                    conversationId: conversationId
                }).then(res => {
                    this.messages.push({
                        isAnswer: true,
                        content: res.answer
                    })
                    this.totalTokens = res.totalTokens
                    this.question = ""
                    this.updateConversationTitle(conversationId)
                }).catch((err) => {
                    // TODO show error
                    alert(err)
                }).finally(() => {
                    this.working = false
                })
            }
        },
        askWithSse(content, conversationId){
            this.useLoadingState = false
            if (content && conversationId) {
                this.lastContentHeight = this.getContentHeight()
                this.messages.push({
                    isAnswer: false,
                    content
                })
                this.working = true
                let index = this.messages.length
                this.messages.push({
                    isAnswer: true,
                    content: ""
                })
                const evtSource = new EventSource(
                    encodeURI(
                        "/chat/sseAsk/" + conversationId + "/" + content
                    ),
                    {
                        withCredentials: true
                    }
                )
                evtSource.onmessage = evt => {
                    const { data } = evt
                    if (data === "[DONE]") {
                        this.flushContent()
                        this.working = false
                        this.question = ""
                        evtSource.close()
                        this.updateConversationTitle(conversationId)
                    } else {
                        let res = JSON.parse(data)
                        this.messages[index].content += res.answer || ""
                    }
                }
                evtSource.onerror = evt => {
                    this.working = false
                    alert("error: " + JSON.stringify(evt))
                    if (evtSource.readyState === EventSource.CLOSED) {
                        console.log("close")
                    } else {
                        evtSource.close()
                    }
                }
            }
        },
        switchConversation(id) {
            const { clear, now } = this
            get("/context/get/" + id).then(data => {
                clear()
                this.conversationId = id
                data.messages.map(m => {
                    if (m.role !== "system") {
                        this.messages.push({
                            isAnswer: m.role === "assistant",
                            content: m.content,
                            time: now(new Date(m.createTime))
                        })
                    }
                })
                this.totalTokens = data.totalTokens
                this.working = false
            }).catch(err => {
                alert(err)
            })
        },
        deleteConversation() {
            const { conversationId, clear } = this
            const { message } = antd
            if (conversationId) {
                get("/context/delete/" + conversationId).then(() => {
                    clear()
                    this.conversationList = this.conversationList.filter(v => v.contextId !== conversationId)
                }).catch(err => {
                    alert(err)
                })
            }
        },
        prefixTime(value) {
            return value < 10 ? "0" + value : value + ""
        },
        now(date) {
            const { prefixTime } = this
            const now = date || new Date()
            return `${now.getFullYear()}-${prefixTime(now.getMonth() + 1)}-${prefixTime(now.getDate())} ${prefixTime(now.getHours())}:${prefixTime(now.getMinutes())}`
            // return (now.getHours() < 10 ? "0" + now.getHours() : now.getHours()) + ":"
            //     + (now.getMinutes() < 10 ? "0" + now.getMinutes() : now.getMinutes())
        },
        today() {
            const date = new Date()
            let m = date.getMonth() + 1
            return date.getFullYear() + "-" + (m < 10 ? "0" + m : m) + "-"
                + (date.getDate() < 10 ? "0" + date.getDate() : date.getDate())
                + " " + this.now(date)
        }
    }
}
</script>
