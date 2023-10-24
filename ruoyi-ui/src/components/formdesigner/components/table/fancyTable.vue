<template>
  <div>
    <div style="padding:5px;margin-top:10px">
      <table class="table-layout" :style="tableStyle">
        <tbody>
          <tr v-for="(tr,trIndex) in layoutArray" :key="trIndex" :style="trHeight">
            <fancy-table-item v-for="(td,tdIndex) in tr" :key="tdIndex" 
              :item="td" 
              :tdIndex="tdIndex" 
              :trIndex="trIndex" 
              :tdStyle="tdStyle" 
              @rightClick="rightClick"
              @click.native="handlerSelectedTd($event,td)" >
              <slot :td="td" />
            </fancy-table-item>
          </tr>
        </tbody>
      </table>
    </div>
    <div v-show="showContextMenu" class="right-menu" :style="{ top:  + positionY+'px',left:  + positionX+'px', }">
      <ul style="list-style-type: none">
        <li @click="handlerRightCol"   v-if="showRightColMenu">
          <icon code="zuoyouhebing"    text="向右合并单元格" />
        </li>
        <li @click="handlerDownRow"    v-if="showDownRowMenu">
          <icon code="shangxiahebing"  text="向下合并单元格" />
        </li>
        <li @click="handlerResetTable" v-if="showResetTableMenu">
          <icon code="chaifen"         text="拆分单元格" />
        </li>
        <li @click="handlerAppendRowBefore">
          <icon code="charuhang"       text="在上方插入行" />
        </li>
        <li @click="handlerAppendRowAfter">
          <icon code="charuhang"       text="在下方插入行" />
        </li>
        <li @click="handlerDeleteRow">
          <icon code="shanchuhang"     text="删除行" />
        </li>
        <li @click="handlerAppendColLeft">
          <icon code="charulie"        text="在左侧插入列" />
        </li>
        <li @click="handlerAppendColRight">
          <icon code="charulie"        text="在右侧插入列" />
        </li>
        <li @click="handlerDeleteCol">
          <icon code="shanchulie"      text="删除列" />
        </li>
      </ul>
    </div>
  </div>
</template>

<script>
import   fancyTableItem        from './fancyTableItem';
import   icon                  from '../icon';
import { jsonClone           } from "../utils";
import { getSimpleId         } from '../utils/IdGenerate';
import { getTrItem,getTdItem } from "./tableFunc";

export default {
  name:'fancyTable',
  components:{
    icon,
    fancyTableItem
  },
  props:{
    layoutArray:{
      type    : Array,
      default : ()=>[]
    },
    tdStyle:{
      type    : String,
      default : ''
    },
    width:{
      type    : Number,
      default : 100
    },
    height:{
      type    : Number,
      default : 100
    }
  },
  data(){
    return{
      tr              : [],
      positionX       : 0,
      positionY       : 0,
      currentRowIndex : 0,
      currentColIndex : 0,
      showContextMenu : false,
    }
  },
  mounted() {
    // 添加监听取消右键菜单
    document.addEventListener(   "click",      this.hideRightContextMenu, true);
    document.addEventListener(   "contextmenu",this.hideRightContextMenu, true);
    this.tr = getTrItem();
    // this.handlerAppendCol();
    // this.handlerAppendCol();
  },
  destroyed() {
    // 移除监听
    document.removeEventListener("click",      this.hideRightContextMenu, true);
    document.removeEventListener("contextmenu",this.hideRightContextMenu, true);
  },
  methods:{
    rightClick(e,rowIndex,colIndex){
      this.showContextMenu = true;
      this.positionX       = e.clientX;
      this.positionY       = e.clientY;
      this.currentRowIndex = rowIndex;
      this.currentColIndex = colIndex;
    },
    hideRightContextMenu(){
      this.showContextMenu = false;
    },
    //向右合并单元格
    handlerRightCol(){
      let col = this.layoutArray[this.currentRowIndex][this.currentColIndex].col;
      let row = this.layoutArray[this.currentRowIndex][this.currentColIndex].row;
      if( row>1 ){
        for(let i =0;i<row;i++){
          this.layoutArray[this.currentRowIndex+i][this.currentColIndex+col].hide=true;
          this.layoutArray[this.currentRowIndex][this.currentColIndex].col=col+1; 
        }
      }else{
        this.layoutArray[this.currentRowIndex][this.currentColIndex+col].hide=true;
        this.layoutArray[this.currentRowIndex][this.currentColIndex].col=col+1;
      }
    },
    //向下合并单元格
    handlerDownRow(){
      let col = this.layoutArray[this.currentRowIndex][this.currentColIndex].col;
      let row = this.layoutArray[this.currentRowIndex][this.currentColIndex].row;
      if( col>1 ){
        for(let i =0;i<col;i++){
          this.layoutArray[this.currentRowIndex+row][this.currentColIndex+i].hide=true;
          this.layoutArray[this.currentRowIndex][this.currentColIndex].row=row+1;
        }
      }else{
        this.layoutArray[this.currentRowIndex+row][this.currentColIndex].hide=true;
        this.layoutArray[this.currentRowIndex][this.currentColIndex].row=row+1;
      }
    },
    handlerResetTable(){
      //debugger;
      let col = this.layoutArray[this.currentRowIndex][this.currentColIndex].col;
      let row = this.layoutArray[this.currentRowIndex][this.currentColIndex].row;
      if(col===1&&row===1)return;

      for(let i = 0;i<row;i++){
        for(let j = 0;j<col;j++){
          this.layoutArray[this.currentRowIndex+i][this.currentColIndex+j].hide = false;
        }
      }
      this.layoutArray[this.currentRowIndex][this.currentColIndex].row=1;
      this.layoutArray[this.currentRowIndex][this.currentColIndex].col=1;
    },
    handlerSelectedTd(e,td){
      this.$emit('selectItem',td);
      e.stopPropagation();
    },
    //在上方插入行
    handlerAppendRowBefore(){
      let _currTr = this.layoutArray[this.currentRowIndex]
      let _trItem = jsonClone(_currTr);
      _trItem.id  = getSimpleId()
      _trItem.map(item=>{
        item.id  = getSimpleId()
      })
      this.layoutArray.splice(this.currentRowIndex,0,_trItem)
      /*
      _trItem.map(item=>item.id=getSimpleId());
      this.layoutArray.push(_trItem);
      */
    },
    //在下方插入行
    handlerAppendRowAfter(){
      let _currTr = this.layoutArray[this.currentRowIndex]
      let _trItem = jsonClone(_currTr);
      _trItem.id  = getSimpleId()
      _trItem.map(item=>{
        item.id  = getSimpleId()
      })
      this.layoutArray.splice(this.currentRowIndex+1,0,_trItem)
      /*
      _trItem.map(item=>item.id=getSimpleId());
      this.layoutArray.push(_trItem);
      */
    },
    //在左侧插入列
    handlerAppendColLeft(){
      let _currTd = this.layoutArray[this.currentRowIndex][this.currentColIndex]
      //this.tr.push(getTdItem());
      this.layoutArray.forEach(item=>{
        const _td = jsonClone(_currTd);
        _td.id    = getSimpleId()
        item.splice(this.currentColIndex,0,_td)
        //item.push(_td);
      })
    },
    //在右侧插入列
    handlerAppendColRight(){
      let _currTd = this.layoutArray[this.currentRowIndex][this.currentColIndex]
      this.layoutArray.forEach(item=>{
        const _td = jsonClone(_currTd);
        _td.id    = getSimpleId()
        item.splice(this.currentColIndex+1,0,_td)
        //item.push(_td);
      })
    },
    handlerDeleteRow(){
      //console.log(this.currentRowIndex,this.currentColIndex)
      if(this.layoutArray.length>1){
        this.layoutArray.splice(this.currentRowIndex,1)
      }else{
        this.$message.error('不能删除表格最后一行');
      }
    },
    handlerDeleteCol(){
      //console.log(this.currentRowIndex,this.currentColIndex)
      this.layoutArray.forEach(row=>{
        if(row.length>1){
          row.splice(this.currentColIndex,1)
        }else{
          this.$message.error('不能删除表格最后一列');
        }
      })
    }
  },
  computed:{
    showRightColMenu(){
      if(this.showContextMenu){
        const col = this.layoutArray[this.currentRowIndex][this.currentColIndex].col;
        const td  = this.layoutArray[this.currentRowIndex][this.currentColIndex+col];
        return (td&&td.row<2&&td.col<2&&!td.hide);
      }else{
        return false;
      }
    },
    showDownRowMenu(){
      if(this.showContextMenu){
        const row = this.layoutArray[this.currentRowIndex][this.currentColIndex].row;
        let    td = undefined;
        if(typeof this.layoutArray[this.currentRowIndex+row] !== 'undefined'){
          td = this.layoutArray[this.currentRowIndex+row][this.currentColIndex];
        }
        return (td&&td.row<2&&td.col<2&&!td.hide);
      }else{
        return false;
      }
    },
    showResetTableMenu(){
      if(this.showContextMenu){
        const td = this.layoutArray[this.currentRowIndex][this.currentColIndex];
        return !(td.row<2&&td.col<2&&!td.hide);
      }else{
        return false;
      }
    },
    tableStyle(){
      return 'width:'  + this.width + '%;';
    },
    trHeight(){
      return 'height:' + this.height+ 'px';
    }
  }
}
</script>

<style scoped>
table{
  border-spacing:0;
  width:100%
}
tbody{
  display: table-row-group;
  vertical-align: middle;
  border-color: inherit;
}
.table-layout{
  background-color: #ffffff;
  border-collapse: collapse;

  padding: 8px !important;
  text-align: left;
  margin: 0 auto;
  width: 100%; 
  table-layout: fixed;
}

.table-layout > tbody > tr{
  border-bottom: 1px solid #d2d2d2;
  border-top: 1px solid #d2d2d2;
}
.right-menu{
  background-color:#ffffff;
  z-index:100;
  width:200px;
  position: fixed;
  border: 1px solid #ccc;
  box-shadow: 3px 3px 8px #999;
  border-radius: 3px;
  padding: 8px 0;
}
.right-menu ul{
  margin:0px;
  padding:0px;
}
.right-menu ul li{
  padding:0 15px;
  height:30px;
  line-height: 30px;
}
.right-menu ul li:hover{
  cursor: pointer;
  background-color: #ccc;
}

</style>