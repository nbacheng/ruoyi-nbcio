<template>
  <div>
    <a-popover trigger="click" placement="bottomRight" :autoAdjustOverflow="true" :arrowPointAtCenter="true"
      overlayClassName="header-notice-wrapper" @visibleChange="handleHoverChange"
      :overlayStyle="{ width: '400px', top: '50px' }">
      <template slot="content">
        <a-spin :spinning="loadding">
          <a-tabs>
            <a-tab-pane :tab="msg1Title" key="1">
              <a-list>
                <a-list-item :key="index" v-for="(record, index) in notice1">
                  <div style="margin-left: 5%;width: 50%">
                    <p><a @click="showNotice(record)">{{ record.titile }}</a></p>
                    <p style="color: rgba(0,0,0,.45);margin-bottom: 0px">{{ record.createTime }} 发布</p>
                  </div>
                  <div style="text-align: right">
                    <a-tag @click="showNotice(record)" v-if="record.priority === 'L'" color="blue">一般消息</a-tag>
                    <a-tag @click="showNotice(record)" v-if="record.priority === 'M'" color="orange">重要消息</a-tag>
                    <a-tag @click="showNotice(record)" v-if="record.priority === 'H'" color="red">紧急消息</a-tag>
                  </div>
                </a-list-item>
                <div style="margin-top: 5px;text-align: center">
                  <a-button @click="toMyNotice()" type="dashed" block>查看更多</a-button>
                </div>
              </a-list>
            </a-tab-pane>
            <a-tab-pane :tab="msg2Title" key="2">
              <a-list>
                <a-list-item :key="index" v-for="(record, index) in notice2">
                  <div style="margin-left: 5%;width: 50%">
                    <p><a @click="showNotice(record)">{{ record.titile }}</a></p>
                    <p style="color: rgba(0,0,0,.45);margin-bottom: 0px">{{ record.createTime }} 发布</p>
                  </div>
                  <div style="text-align: right">
                    <a-tag @click="showNotice(record)" v-if="record.priority === 'L'" color="blue">一般消息</a-tag>
                    <a-tag @click="showNotice(record)" v-if="record.priority === 'M'" color="orange">重要消息</a-tag>
                    <a-tag @click="showNotice(record)" v-if="record.priority === 'H'" color="red">紧急消息</a-tag>
                  </div>
                </a-list-item>
                <div style="margin-top: 5px;text-align: center">
                  <a-button @click="toMyNotice()" type="dashed" block>查看更多</a-button>
                </div>
              </a-list>
            </a-tab-pane>
            <a-tab-pane :tab="msg3Title" key="3">
              <a-list>
                <a-list-item :key="index" v-for="(record, index) in notice3">
                  <div style="margin-left: 5%;width: 50%">
                    <p><a @click="showNotice(record)">{{ record.titile }}</a></p>
                    <p style="color: rgba(0,0,0,.45);margin-bottom: 0px">{{ record.createTime }} 发布</p>
                  </div>
                  <div style="text-align: right">
                    <a-tag @click="showNotice(record)" v-if="record.priority === 'L'" color="blue">一般消息</a-tag>
                    <a-tag @click="showNotice(record)" v-if="record.priority === 'M'" color="orange">重要消息</a-tag>
                    <a-tag @click="showNotice(record)" v-if="record.priority === 'H'" color="red">紧急消息</a-tag>
                  </div>
                </a-list-item>
                <div style="margin-top: 5px;text-align: center">
                  <a-button @click="toMyNotice()" type="dashed" block>查看更多</a-button>
                </div>
              </a-list>
            </a-tab-pane>
          </a-tabs>
        </a-spin>
      </template>
      <span @click="fetchNotice" class="header-notice">
        <a-badge :count="msgTotal">
          <a-icon style="font-size: 16px; padding: 4px" type="bell" />
        </a-badge>
      </span>
      <show-notice ref="ShowNotice" @ok="modalFormOk"></show-notice>
      <dynamic-notice ref="showDynamNotice" :path="openPath" :formData="formData" />
    </a-popover>
  </div>
</template>

<script>
  import ShowNotice from './ShowNotice'
  import store from '@/store/'
  import DynamicNotice from './DynamicNotice'
  import { listByUser, updateUserIdAndNotice } from "@/api/system/notice";

  export default {
    name: "HeaderNotice",
    components: {
      DynamicNotice,
      ShowNotice,
    },
    data() {
      return {
        loadding: false,
        hovered: false,
        notice1: [],
        notice2: [],
        notice3: [],
        msg1Count: "0",
        msg2Count: "0",
        msg3Count: "0",
        msg1Title: "通知(0)",
        msg2Title: "",
        msg3Title: "",
        stopTimer: false,
        websock: null,
        lockReconnect: false,
        heartCheck: null,
        formData: {},
        openPath: ''
      }
    },
    computed: {
      msgTotal() {
        return parseInt(this.msg1Count) + parseInt(this.msg2Count) + parseInt(this.msg3Count);
      }
    },
    mounted() {
      this.loadData();
      //this.timerFun();
      this.initWebSocket();
      // this.heartCheckFun();
    },
    destroyed: function() { // 离开页面生命周期函数
      this.websocketOnclose();
    },
    methods: {
      timerFun() {
        this.stopTimer = false;
        let myTimer = setInterval(() => {
          // 停止定时器
          if (this.stopTimer == true) {
            clearInterval(myTimer);
            return;
          }
          this.loadData()
        }, 6000)
      },
      loadData() {
        try {
          // 获取系统消息
          listByUser().then((res) => {
            console.log("listByUser res",res);
            if (res.code == 200) {
              this.notice1 = res.data.anntMsgList;
              this.msg1Count = res.data.anntMsgTotal;
              this.msg1Title = "通知(" + res.data.anntMsgTotal + ")";
              this.notice2 = res.data.sysMsgList;
              this.msg2Count = res.data.sysMsgTotal;
              this.msg2Title = "系统消息(" + res.data.sysMsgTotal + ")";
              this.notice3 = res.data.todealMsgList;
              this.msg3Count = res.data.todealMsgTotal;
              this.msg3Title = "待办消息(" + res.data.todealMsgTotal + ")";
            }
          }).catch(error => {
            console.log("系统消息通知异常", error); //这行打印permissionName is undefined
            this.stopTimer = true;
            console.log("清理timer");
          });
        } catch (err) {
          this.stopTimer = true;
          console.log("通知异常", err);
        }
      },
      fetchNotice() {
        if (this.loadding) {
          this.loadding = false
          return
        }
        this.loadding = true
        setTimeout(() => {
          this.loadding = false
        }, 200)
      },
      showNotice(record) {
        updateUserIdAndNotice({
          noticeId: record.noticeId
        }).then((res) => {
          if (res.code == 200) {
            this.loadData();
          }
        });
        this.hovered = false;
        if (record.openType === 'component') {
          this.openPath = record.openPage;
          this.formData = {
            id: record.busId
          };
          this.$refs.showDynamNotice.detail(record.openPage);
        } else {
          this.$refs.ShowNotice.detail(record);
        }
      },
      toMyNotice() {
        this.$router.push({
          path: '/personal/mynotice'
        });
      },
      modalFormOk() {},
      handleHoverChange(visible) {
        this.hovered = visible;
      },

      initWebSocket: function() {
        // WebSocket与普通的请求所用协议有所不同，ws等同于http，wss等同于https
        var uid = store.getters.name;
        var url = process.env.VUE_APP_WS_API + "/websocket/" + uid;
        console.log("url=",url);
        this.websock = new WebSocket(url);
        this.websock.onopen = this.websocketOnopen;
        this.websock.onerror = this.websocketOnerror;
        this.websock.onmessage = this.websocketOnmessage;
        this.websock.onclose = this.websocketOnclose;
      },
      websocketOnopen: function() {
        console.log("WebSocket连接成功");
        //心跳检测重置
        //this.heartCheck.reset().start();
      },
      websocketOnerror: function(e) {
        console.log("WebSocket连接发生错误");
        this.reconnect();
      },
      websocketOnmessage: function(e) {
        console.log("-----接收消息-------", e);
        console.log("-----接收消息-------", e.data);
        var data = eval("(" + e.data + ")"); //解析对象
        if (data.cmd == "topic") {
          //系统通知
          this.loadData();
          this.$notification.open({ //websocket消息通知弹出
            message: 'websocket消息通知',
            description: data.msgTxt,
            style: {
              width: '600px',
              marginLeft: `${335 - 600}px`,
            },
          });
        } else if (data.cmd == "user") {
          //用户消息
          this.loadData();
          this.$notification.open({
            message: 'websocket消息通知',
            description: data.msgTxt,
            style: {
              width: '600px',
              marginLeft: `${335 - 600}px`,
            },
          });
        }
        //心跳检测重置
        //this.heartCheck.reset().start();
      },
      websocketOnclose: function(e) {
        console.log("connection closed (" + e + ")");
        if (e) {
          console.log("connection closed (" + e.code + ")");
        }
        this.reconnect();
      },
      websocketSend(text) { // 数据发送
        try {
          this.websock.send(text);
        } catch (err) {
          console.log("send failed (" + err.code + ")");
        }
      },

      openNotification(data) {
        var text = data.msgTxt;
        const key = `open${Date.now()}`;
        this.$notification.open({
          message: '消息提醒',
          placement: 'bottomRight',
          description: text,
          key,
          btn: (h) => {
            return h('a-button', {
              props: {
                type: 'primary',
                size: 'small',
              },
              on: {
                click: () => this.showDetail(key, data)
              }
            }, '查看详情')
          },
        });
      },

      reconnect() {
        var that = this;
        if (that.lockReconnect) return;
        that.lockReconnect = true;
        //没连接上会一直重连，设置延迟避免请求过多
        setTimeout(function() {
          console.info("尝试重连...");
          that.initWebSocket();
          that.lockReconnect = false;
        }, 5000);
      },
      heartCheckFun() {
        var that = this;
        //心跳检测,每20s心跳一次
        that.heartCheck = {
          timeout: 20000,
          timeoutObj: null,
          serverTimeoutObj: null,
          reset: function() {
            clearTimeout(this.timeoutObj);
            //clearTimeout(this.serverTimeoutObj);
            return this;
          },
          start: function() {
            var self = this;
            this.timeoutObj = setTimeout(function() {
              //这里发送一个心跳，后端收到后，返回一个心跳消息，
              //onmessage拿到返回的心跳就说明连接正常
              that.websocketSend("HeartBeat");
              console.info("客户端发送心跳");
              //self.serverTimeoutObj = setTimeout(function(){//如果超过一定时间还没重置，说明后端主动断开了
              //  that.websock.close();//如果onclose会执行reconnect，我们执行ws.close()就行了.如果直接执行reconnect 会触发onclose导致重连两次
              //}, self.timeout)
            }, this.timeout)
          }
        }
      },


      showDetail(key, data) {
        this.$notification.close(key);
        var id = data.msgId;
        getAction(this.url.queryById, {
          id: id
        }).then((res) => {
          if (res.success) {
            var record = res.result;
            this.showNotice(record);
          }
        })

      },
    }
  }
</script>

<style lang="css">
  .header-notice-wrapper {
    top: 50px !important;
  }
</style>
<style lang="less" scoped>
  .header-notice {
    display: inline-block;
    transition: all 0.3s;

    span {
      vertical-align: initial;
    }
  }
</style>
