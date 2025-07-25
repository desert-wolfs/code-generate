package com.douniu.codegenerate;

import com.douniu.codegenerate.utils.StrUtil;
import org.mybatis.generator.api.*;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.config.*;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.exception.InvalidConfigurationException;
import org.mybatis.generator.exception.XMLParserException;
import org.mybatis.generator.internal.DefaultShellCallback;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.douniu.codegenerate.controller.GenCodeController.GEN_TARGET;

public class MybatisGeneratorRunner {
    // 方式一： xml配置形式
//    public static void main(String[] args) throws XMLParserException, IOException, InvalidConfigurationException, SQLException, InterruptedException {
//        List<String> warnings = new ArrayList<String>();
//        boolean overwrite = true;
//        // 使用xml配置文件的方式
//        File configFile = new File(MybatisGeneratorRunner1.class.getClassLoader().getResource("generatorConfig.xml").getPath());
//        ConfigurationParser cp = new ConfigurationParser(warnings);
//        Configuration config = cp.parseConfiguration(configFile);
//        // 使用纯Java代码配置的方式, 相当于把 generatorConfig.xml配置的都用java代码配置到config中
//        // config = new Configuration();
//
//        DefaultShellCallback callback = new DefaultShellCallback(overwrite);
//        MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config, callback, warnings);
//        myBatisGenerator.generate(null);
//        System.out.println(warnings);
//    }


    // 方式二： xml转成Configuration形式
    /**
     * 方式二： xml转成Configuration形式
     * @param args 表名 数组
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        List<String> warnings = new ArrayList<>();

        // 创建配置对象
        Configuration config = new Configuration();

        // 创建上下文
        Context context = new Context(ModelType.CONDITIONAL);
        context.setId("simple");
        context.setTargetRuntime("MyBatis3");

        // 设置属性
        context.addProperty("javaFileEncoding", "UTF-8");

        // 添加插件
        context.addPluginConfiguration(createPluginConfig("org.mybatis.generator.plugins.UnmergeableXmlMappersPlugin"));
        context.addPluginConfiguration(createPluginConfig("com.douniu.codegenerate.plugin.LombokPlugin"));
        context.addPluginConfiguration(createPluginConfig("com.douniu.codegenerate.plugin.DaoMapperPlugin"));
        context.addPluginConfiguration(createPluginConfig("com.douniu.codegenerate.plugin.InsertKeyPlugin"));

        // 配置注释生成器
        CommentGeneratorConfiguration commentConfig = new CommentGeneratorConfiguration();
        commentConfig.setConfigurationType("com.douniu.codegenerate.plugin.CustomCommentGenerator");
        commentConfig.addProperty("suppressAllComments", "true");
        context.setCommentGeneratorConfiguration(commentConfig);

        // 配置数据库连接
        JDBCConnectionConfiguration jdbcConfig = new JDBCConnectionConfiguration();
        jdbcConfig.setDriverClass("com.mysql.cj.jdbc.Driver");
        jdbcConfig.setConnectionURL("jdbc:mysql://192.168.2.74:3306/newfaceshow?useUnicode=true&characterEncoding=utf8&tinyInt1isBit=true&serverTimezone=UTC");
        jdbcConfig.setUserId("root");
        jdbcConfig.setPassword("root");
        jdbcConfig.addProperty("nullCatalogMeansCurrent", "true");
        context.setJdbcConnectionConfiguration(jdbcConfig);

        // 配置字段自定义转换
        JavaTypeResolverConfiguration javaTypeResolverConfig = new JavaTypeResolverConfiguration();
        javaTypeResolverConfig.setConfigurationType("com.douniu.codegenerate.plugin.CustomTypeResolver");
        context.setJavaTypeResolverConfiguration(javaTypeResolverConfig);
        File rootDirectory = new File(GEN_TARGET);
        if (!rootDirectory.exists()) {
            rootDirectory.mkdirs();
        }

        // 配置生成 PO 的包名和位置
        JavaModelGeneratorConfiguration javaModelConfig = new JavaModelGeneratorConfiguration();
        javaModelConfig.setTargetPackage("entity");
        javaModelConfig.setTargetProject(GEN_TARGET);
        context.setJavaModelGeneratorConfiguration(javaModelConfig);

        // 配置生成 XML 映射文件的包名和位置
        SqlMapGeneratorConfiguration sqlMapConfig = new SqlMapGeneratorConfiguration();
        sqlMapConfig.setTargetPackage("mapper");
        sqlMapConfig.setTargetProject(GEN_TARGET);
        context.setSqlMapGeneratorConfiguration(sqlMapConfig);

        // 配置生成 Mapper 接口的包名和位置
        JavaClientGeneratorConfiguration javaClientConfig = new JavaClientGeneratorConfiguration();
        javaClientConfig.setConfigurationType("XMLMAPPER");
        javaClientConfig.setTargetPackage("dao");
        javaClientConfig.setTargetProject(GEN_TARGET);
        context.setJavaClientGeneratorConfiguration(javaClientConfig);

        for (String tableName : args) {
            genOneTable(context, config, tableName, warnings);
        }
    }

    private static void genOneTable(Context context, Configuration config, String tableName, List<String> warnings) throws InvalidConfigurationException, SQLException, IOException, InterruptedException {
        // 配置表信息
        TableConfiguration tableConfig = new TableConfiguration(context);
        tableConfig.setTableName(tableName);
        tableConfig.setDomainObjectName(StrUtil.toPascalCase(tableName));
        tableConfig.setCountByExampleStatementEnabled(false);
        tableConfig.setDeleteByExampleStatementEnabled(false);
        tableConfig.setSelectByExampleStatementEnabled(false);
        tableConfig.setUpdateByExampleStatementEnabled(false);

        // 自增主键列（注释部分对应 XML 注释）
        // GeneratedKey generatedKey = new GeneratedKey("id", "MYSQL", true, null);
        // tableConfig.setGeneratedKey(generatedKey);

        // tinyint 映射为 Integer（注释部分对应 XML 注释）
        // ColumnOverride columnOverride = new ColumnOverride("log_type");
        // columnOverride.setJavaType(new FullyQualifiedJavaType(Integer.class.getName()));
        // columnOverride.setJdbcType("TINYINT");
        // tableConfig.addColumnOverride(columnOverride);

        context.addTableConfiguration(tableConfig);

        config.addContext(context);

        // 默认覆盖
        boolean overwrite = true;
        DefaultShellCallback callback = new DefaultShellCallback(overwrite);
        MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config, callback, warnings);
        myBatisGenerator.generate(null);
        System.out.println(warnings);
    }

    private static PluginConfiguration createPluginConfig(String type) {
        PluginConfiguration pluginConfig = new PluginConfiguration();
        pluginConfig.setConfigurationType(type);
        return pluginConfig;
    }
}