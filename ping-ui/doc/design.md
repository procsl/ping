# 抽象组件设计

#### 组件设计

> 抽象组件使用`json schema`描述组件的功能骨架, 以实现布局与样式分离

1. 任意组件需要具有以下2个属性

    ```text
    String  id;     // 唯一Key,用来唯一标识一个组件
    String  name;   // 组件名称，用来标识一种组件类型
    ```

2. 组件具有事件属性,可以监听指定事件并注册

#### 组件定义

> 以下是使用`json schema`定义的组件,可以自由扩充其属性与子节点

1. 应用组件(Application)

   ```text
   Array container;   // 容器
   ```

2. 菜单组件(Menu)

    ```text
    Array  children; // 菜单子节点,菜单子节点的顺序为数组中的顺序
    ```

3. 表单输入组件(input)
    ```text

    ```
