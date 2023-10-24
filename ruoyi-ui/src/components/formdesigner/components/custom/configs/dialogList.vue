<template>
  <div v-show="props.compType === 'dialogList'" class="dialogList">
    <el-collapse v-model="activePanel" accordion>
      <el-collapse-item title="基础设置" name="1">
        <el-form-item label="ID">
          <el-tooltip
            class="item"
            effect="dark"
            content="请注意,ID的修改可能会导致该组件相关事件失效！"
            placement="left"
          >
            <el-input class="input" v-model="props.id"></el-input>
          </el-tooltip>
        </el-form-item>
        <el-form-item label="标题">
          <el-input class="input" v-model="props.label"></el-input>
        </el-form-item>
        <el-form-item label="栅格">
          <el-input-number v-model="props.span" :min="1" :max="24" />
        </el-form-item>
        <el-form-item label="标签宽度">
          <el-input-number
            v-model="props.labelWidth"
            :min="1"
            :max="200"
          ></el-input-number>
        </el-form-item>
        <el-form-item label="显示标签">
          <el-switch v-model="props.showLabel"></el-switch>
        </el-form-item>
        <el-form-item label="必填">
          <el-switch v-model="props.required"></el-switch>
        </el-form-item>
        <el-form-item label="禁用">
          <el-switch v-model="props.disabled"></el-switch>
        </el-form-item>
        <el-form-item label="默认值">
          <el-input class="input" v-model="props.value"></el-input>
        </el-form-item>
        <el-form-item label="请求地址">
          <el-input v-model="props.action"></el-input>
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="props.title"></el-input>
        </el-form-item>
        <el-form-item label="多选">
          <el-switch v-model="props.multi"></el-switch>
        </el-form-item>
        <el-form-item label="显示序号">
          <el-switch v-model="props.showIndex"></el-switch>
        </el-form-item>
        <el-form-item label="搜索">
          <el-switch v-model="props.searchable"></el-switch>
        </el-form-item>
        <el-form-item label="表格高度">
          <el-input-number
            v-model="props.height"
            :step="10"
            :max="1500"
            :min="100"
          ></el-input-number>
        </el-form-item>
        <el-form-item label="字段值">
          <el-input v-model="props.dval"></el-input>
        </el-form-item>
        <el-form-item label="字段名称">
          <el-input v-model="props.dlabel"></el-input>
        </el-form-item>
        <custom-style :formConf="formConf" :data="props"></custom-style>
        <el-form-item label="">
          <el-button type="primary" plain @click="ColItemConfig"> 字段配置 </el-button>
        </el-form-item>

      </el-collapse-item>
      <el-collapse-item title="数值联动" name="2"> </el-collapse-item>
    </el-collapse>
    <el-dialog
      :visible.sync="dialogVisible"
      title="字段配置"
      width="1200px"
      :close-on-click-modal="false"
    >
      <el-table
        :data="colOptions"
        border
        style="width: 100%"
        :header-cell-style="{ background: '#F5F7FA' }"
        :fit="true"
      >
        <el-table-column prop="label" label="字段名">
          <template slot-scope="scope">
            <el-input v-model="scope.row.label" />
          </template>
        </el-table-column>
        <el-table-column prop="property" label="属性名">
          <template slot-scope="scope">
            <el-input v-model="scope.row.property"
          /></template>
        </el-table-column>
        <el-table-column prop="width" label="宽度" width="220">
          <template slot-scope="scope">
            <el-input-number v-model="scope.row.width"
          /></template>
        </el-table-column>
        <el-table-column prop="show" label="显示" width="100">
          <template slot-scope="scope"> <el-switch v-model="scope.row.show" /></template>
        </el-table-column>
        <el-table-column prop="search" label="搜索" width="100">
          <template slot-scope="scope">
            <el-switch v-model="scope.row.search"
          /></template>
        </el-table-column>
        <el-table-column prop="search" label="操作" width="100">
          <template slot="header" slot-scope="scope">
            <i
              class="el-icon-circle-plus"
              @click="handleAdd"
              style="font-size: 20px; margin-top: 5px; cursor: pointer"
            ></i>
          </template>
          <template slot-scope="scope">
            <el-button type="text" @click="handleDelete(scope.$index)"
              ><i class="el-icon-lx-deletefill" />删除</el-button
            >
          </template>
        </el-table-column>
      </el-table>
      <span slot="footer" class="dialog-footer">
        <el-button @click="handlerClose">取 消</el-button>
        <el-button type="primary" @click="handlerSubmit">确 定</el-button>
      </span>
    </el-dialog>
  </div>
</template>
<script>
  import customStyle from '../css/customStyle'
/**
 * dialogList的配置项
 */
const row = {
  label: "",
  property: "",
  width: undefined,
  show: true,
  search: true,
};
export default {
  name: "dialogConfig",
  props: ['props','formConf'],
  components:{customStyle},
  data() {
    return {
      activePanel: "1",
      colOptions: [],
      dLabel: "",
      dProperty: "",
      dWidth: 150,
      dShow: true,
      alertShow: false,
      propertyExistShow: false,
      dialogVisible: false,
    };
  },
  methods: {
    handleAdd() {
      this.colOptions.push(row);
    },
    handleDelete(index) {
      this.colOptions.splice(index, 1);
    },
    ColItemConfig() {
      this.dialogVisible = true;
      console.log(this.colOptions);
    },
    resetFields() {
      this.dLabel = "";
      this.dProperty = "";
      this.dWidth = 150;
      this.dShow = true;
    },
    handlerDeleteRow(row) {
      let index = this.colOptions.findIndex((item) => item.property == row.property);
      this.colOptions.splice(index, 1);
    },
    handlerSubmit() {
      this.colOptions.forEach((item) => {
        if (item.width === "0") {
          item.width = "";
        }
      });
      this.props.columnConf = JSON.stringify(this.colOptions);
      this.dialogVisible = false;
    },
    handlerClose() {
      this.colOptions = [].concat(JSON.parse(this.props.columnConf));
      this.dialogVisible = false;
    },
  },
  mounted() {
    this.$nextTick(() => {
      console.log(this.props);
      this.colOptions = [].concat(JSON.parse(this.props.columnConf));
      console.log(this.colOptions);
    });
  },
};
</script>
<style scoped>
.dialogList >>> .el-collapse-item__header {
  background-color: #f4f6fc;
  padding-left: 10px;
}
.dialogList >>> .el-collapse-item__header {
  height: 35px;
}
</style>
