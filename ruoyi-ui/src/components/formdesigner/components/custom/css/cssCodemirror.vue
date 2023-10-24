<template>
    <div style="line-height: 25px;">
        <codemirror v-model="formConf.classStyle" :options="options"/>
    </div>
</template>

<script>
    import {codemirror} from 'vue-codemirror';
    // 核心样式
    import 'codemirror/lib/codemirror.css';
    // 引入主题后还需要在 options 中指定主题才会生效
    import 'codemirror/theme/dracula.css';
    import 'codemirror/mode/javascript/javascript'
    import {removeStyle, addStyle, getClassCss} from '../../utils'

    const css = require('css');

    export default {
        name: "cssCodemirror",
        components: {codemirror},
        props: {
            formConf: {
                type: Object,
                default: {}
            }
        },
        data() {
            return {
                // 默认配置
                options: {
                    line: true,
                    mode: 'text/css', // 数据高亮
                    theme: 'dracula', //设置主题
                    tabSize: 1,
                    lineNumbers: true, // 显示行号
                    cursorHeight: 0.8, //光标高度，默认是1
                    autoCloseBrackets: true,
                    matchBrackets: true, // 括号匹配
                    lineWrapping: 'wrap', // 文字过长时，是换行(wrap)还是滚动(scroll),默认是滚动
                    showCursorWhenSelecting: true, // 文本选中时显示光标
                    smartIndent: true, // 智能缩进
                    completeSingle: false // 当匹配只有一项的时候是否自动补全
                },
                oldClassStyle: {}
            }
        },
        methods: {
            setClassStyle() {
                // 清除上一次css
                let arr = Object.keys(this.oldClassStyle);
                if (arr.length != 0) {
                    let ast = getClassCss(css.parse(this.oldClassStyle))
                    if (ast) {
                        Object.keys(ast).map(key => {
                            removeStyle(key)
                        })
                    }
                }
                // 添加css
                if (this.formConf.classStyle) {
                    this.oldClassStyle = this.formConf.classStyle
                    let ast = getClassCss(css.parse(this.formConf.classStyle))
                    if (ast) {
                        Object.keys(ast).map(key => {
                            addStyle(key, ast[key])
                        })
                    }
                }
            }
        }
    }
</script>
