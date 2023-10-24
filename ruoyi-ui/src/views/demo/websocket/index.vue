<template>
  <div>
    <el-input v-model="url" type="text" style="width: 100%" /> &nbsp; &nbsp;
    <br />
    <el-button @click="join" type="primary">连接</el-button>
    <el-button @click="exit" type="danger">断开</el-button>

    <br />
    <el-input type="textarea" v-model="message" :rows="9" />
    <el-button type="info" @click="send">发送消息</el-button>
    <br />
    <br />
    <el-input type="textarea" v-model="text_content" :rows="9" /> 返回内容
    <br />
    <br />
  </div>
</template>

<script>
export default {
  data() {
    return {
      url: "ws://127.0.0.1:9060/websocket/ry",
      message: "",
      text_content: "",
      ws: null,
    };
  },
  methods: {
    join() {
      const wsuri = this.url;
      this.ws = new WebSocket(wsuri);
      const self = this;
      this.ws.onopen = function (event) {
        self.text_content = self.text_content + "已经打开连接!" + "\n";
      };
      this.ws.onmessage = function (event) {
        self.text_content = event.data + "\n";
      };
      this.ws.onclose = function (event) {
        self.text_content = self.text_content + "已经关闭连接!" + "\n";
      };
    },
    exit() {
      if (this.ws) {
        this.ws.close();
        this.ws = null;
      }
    },
    send() {
      if (this.ws) {
        const messageData = {
          msgTxt: this.message,
          cmd: 'user'
        }
        let strdata = JSON.stringify(messageData);
         console.log("strdata",JSON.stringify(messageData));
        this.ws.send(strdata);
        //this.ws.send(this.message);
      } else {
        alert("未连接到服务器");
      }
    },
  },
};
</script>
