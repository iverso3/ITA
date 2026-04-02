package com.bank.itarch.meta.constant;

/**
 * 元关系类型常量
 */
public interface MetaRelType {

    /** 多对一 */
    String MANY_TO_ONE = "N:1";

    /** 一对多 */
    String ONE_TO_MANY = "1:N";

    /** 多对多 */
    String MANY_TO_MANY = "N:N";

    /** 一对一 */
    String ONE_TO_ONE = "1:1";
}
