# OkDrools
语义化drools工具，通过简单的文字语言公式生成drl文件。

drools的drl文件内容通常不利于非技术人员编写，特别是让业务人员编写非常不便，此工具可以通过让业务
人员编写通俗易懂的中文规则自动生成drl文件帮助计算。

## 关于此规则事项
+ 设定规则依赖参数映射表，即所有的公式中的中文必须对应业务中的ID，你可能需要建立如下表：

|  ID   | NAME  |
|  ----  | ----  |
| 001  | 语文 |
| 002  | 数学 |
| 003  | 英语 |
| 004  | 总成绩 |

+ 公式支持基础四则运算（+、-、*、/），与运算(&&), 或运算(||), IF条件判断语句;
+ 基本计算公式 **a = b + c**;
+ 当a为如上表中维护的参数，并带有[]时，可以通过实现**ExecRecorder**类调用*save*方法获取到计算结果;
+ IF条件判断语句基本格式: IF 条件1 THEN 结果1,结果2 ELSE IF 条件2 THEN 结果3，结果4 ELSE IF ... THEN ... ELSE THEN 其他结果;
+ 中间变量说明：公式支持中文和英文变量，但推荐您使用英文;
+ 优先级：数值越小其优先级越高, 运算时会被先执行;
+ 未使用**ROUND**函数的计算结果一律保留两位小数;

## 创建规则文件例子
设定一套规则，计算学生的语文、数学、英语成绩总和，如下为计算名字为张三
在201908月份的成绩，其中规则为：

[总成绩]=[语文]+[数学]+[英语];

```
OkDroolsConfig info = new ExampleConfig();
String[] names = new String[] {"张三"};
OkDroolsCondition condition = new ExampleCondition(names);

// 1.公式转为对应参数ID
String formula = strToUUID(TEST_FORMULA, true);
// 2.创建规则文件
ExecBaseBody body = new ExecBaseBody();
body.put("period", "201908");
body.put("ruleId", "12345678");

BiMap paramIdMap = HashBiMap.create();
paramIdMap.put("001", "语文");
paramIdMap.put("002", "数学");
paramIdMap.put("003", "英语");
paramIdMap.put("004", "总成绩");
FormulaToDrlConverter converter = new FormulaToDrlConverter(paramIdMap);
converter.createDrl(info, condition, body, 0, formula);
```
