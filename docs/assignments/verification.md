# 四份作业测试结果

来源：本地构建产生的 Surefire XML 与 JaCoCo XML。
对应源码：[ab71b3c32dfa](https://github.com/YOG-Moros/backend-learning/commit/ab71b3c32dfad6a98fe4d70296bb759adb0e461b)。
仅摘录测试名称、结果与覆盖率，不公开原始 XML 中的环境变量和运行日志。
这是构建结果摘要；生成摘要不会重新执行测试。

| 作业 | 测试数 | 失败 | 错误 | 跳过 | 状态 |
| --- | ---: | ---: | ---: | ---: | --- |
| 08-security | 39 | 0 | 0 | 0 | 通过 |
| 10-doc | 19 | 0 | 0 | 0 | 通过 |
| 11-test | 28 | 0 | 0 | 0 | 通过 |
| 12-actuator | 17 | 0 | 0 | 0 | 通过 |

## 08-security

| 测试类（点击查看源码） | 用例数 | 失败 | 错误 | 跳过 |
| --- | ---: | ---: | ---: | ---: |
| [JwtServiceTest](https://github.com/YOG-Moros/backend-learning/blob/ab71b3c32dfad6a98fe4d70296bb759adb0e461b/08-security/src/test/java/top/cch/security/JwtServiceTest.java) | 1 | 0 | 0 | 0 |
| [LessonSecurityIntegrationTest](https://github.com/YOG-Moros/backend-learning/blob/ab71b3c32dfad6a98fe4d70296bb759adb0e461b/08-security/src/test/java/top/cch/security/LessonSecurityIntegrationTest.java) | 9 | 0 | 0 | 0 |
| [SecurityCaseTests](https://github.com/YOG-Moros/backend-learning/blob/ab71b3c32dfad6a98fe4d70296bb759adb0e461b/08-security/src/test/java/top/cch/security/SecurityCaseTests.java) | 22 | 0 | 0 | 0 |
| [InMemorySmsCodeServiceTest](https://github.com/YOG-Moros/backend-learning/blob/ab71b3c32dfad6a98fe4d70296bb759adb0e461b/08-security/src/test/java/top/cch/security/sms/InMemorySmsCodeServiceTest.java) | 4 | 0 | 0 | 0 |
| [RedisSmsCodeServiceTest](https://github.com/YOG-Moros/backend-learning/blob/ab71b3c32dfad6a98fe4d70296bb759adb0e461b/08-security/src/test/java/top/cch/security/sms/RedisSmsCodeServiceTest.java) | 2 | 0 | 0 | 0 |
| [SmsCodeAuthenticationProviderTest](https://github.com/YOG-Moros/backend-learning/blob/ab71b3c32dfad6a98fe4d70296bb759adb0e461b/08-security/src/test/java/top/cch/security/sms/SmsCodeAuthenticationProviderTest.java) | 1 | 0 | 0 | 0 |

<details>
<summary>展开逐项测试结果</summary>

| 用例 | 结果 |
| --- | --- |
| JwtServiceTest · expiredTokensAndDifferentSigningKeysAreRejected | 通过 |
| LessonSecurityIntegrationTest · smsIsOneTimeAndChecksAccountState | 通过 |
| LessonSecurityIntegrationTest · malformedAndTamperedTokensAreUnauthorized | 通过 |
| LessonSecurityIntegrationTest · stageOneRequiresBasicCredentials | 通过 |
| LessonSecurityIntegrationTest · disabledAndLockedAccountsReturnForbidden | 通过 |
| LessonSecurityIntegrationTest · preflightAndUnauthorizedResponseHaveCorsHeaders | 通过 |
| LessonSecurityIntegrationTest · rolesAndPermissionsAreDifferent | 通过 |
| LessonSecurityIntegrationTest · jwtLoginAndAuthorization | 通过 |
| LessonSecurityIntegrationTest · formLoginRequiresCsrfAndCreatesSession | 通过 |
| LessonSecurityIntegrationTest · stageTwoProtectsWritesButAllowsReads | 通过 |
| SecurityCaseTests · pingAnonymousShouldReturn200 | 通过 |
| SecurityCaseTests · noTokenShouldReturn401 | 通过 |
| SecurityCaseTests · studentLoginShouldReturnToken | 通过 |
| SecurityCaseTests · wrongPasswordShouldReturn30001 | 通过 |
| SecurityCaseTests · disabledAccountShouldReturn30002 | 通过 |
| SecurityCaseTests · meShouldReturnIdentityWithAuthorities | 通过 |
| SecurityCaseTests · teacherListAllShouldReturn28Tasks | 通过 |
| SecurityCaseTests · studentListAllShouldReturn403 | 通过 |
| SecurityCaseTests · studentReadOthersTaskShouldReturn403 | 通过 |
| SecurityCaseTests · teacherReadAnyTaskShouldReturn200 | 通过 |
| SecurityCaseTests · studentMineShouldOnlyContainOwnTasks | 通过 |
| SecurityCaseTests · student10SubmitShouldSuccess | 通过 |
| SecurityCaseTests · duplicateSubmitShouldReturn30011 | 通过 |
| SecurityCaseTests · teacherReviewTask1ShouldSuccess | 通过 |
| SecurityCaseTests · duplicateReviewShouldReturn30012 | 通过 |
| SecurityCaseTests · reviewNotExistTaskShouldReturn30010 | 通过 |
| SecurityCaseTests · adminListUsersShouldHidePassword | 通过 |
| SecurityCaseTests · adminCreateUserShouldSuccess | 通过 |
| SecurityCaseTests · adminCreateDuplicateUserShouldReturn30004 | 通过 |
| SecurityCaseTests · adminDeleteOtherUserShouldSuccess | 通过 |
| SecurityCaseTests · adminDeleteSelfShouldReturn30005 | 通过 |
| SecurityCaseTests · teacherAccessAdminShouldReturn403 | 通过 |
| InMemorySmsCodeServiceTest · onlyOneConcurrentVerificationSucceeds | 通过 |
| InMemorySmsCodeServiceTest · wrongAttemptsInvalidateEvenCorrectAnswer | 通过 |
| InMemorySmsCodeServiceTest · rateLimitAndExpiryUseIndependentDeadlines | 通过 |
| InMemorySmsCodeServiceTest · codeIsSixDigitsAndConsumedOnce | 通过 |
| RedisSmsCodeServiceTest · cooldownAndFailedAttemptsAreEnforced | 通过 |
| RedisSmsCodeServiceTest · concurrentConsumersCannotReuseCode | 通过 |
| SmsCodeAuthenticationProviderTest · disabledAndLockedAccountsCannotBypassPasswordProvider | 通过 |

</details>

## 10-doc

| 测试类（点击查看源码） | 用例数 | 失败 | 错误 | 跳过 |
| --- | ---: | ---: | ---: | ---: |
| BookApiTest | 8 | 0 | 0 | 0 |
| OpenApiDocumentTest | 11 | 0 | 0 | 0 |

<details>
<summary>展开逐项测试结果</summary>

| 用例 | 结果 |
| --- | --- |
| 图书接口行为 · createValidationFailed | 通过 |
| 图书接口行为 · boomEndpoint | 通过 |
| 图书接口行为 · getById | 通过 |
| 图书接口行为 · createDuplicateIsbn | 通过 |
| 图书接口行为 · deleteThenNotFound | 通过 |
| 图书接口行为 · filterByKeywordAndCategory | 通过 |
| 图书接口行为 · pageQuery | 通过 |
| 图书接口行为 · createBook | 通过 |
| 接口文档契约 · exportImportableDocuments | 通过 |
| 接口文档契约 · infoMetadata | 通过 |
| 接口文档契约 · requiredFieldsDocumented | 通过 |
| 接口文档契约 · summaryComesFromAnnotation | 通过 |
| 接口文档契约 · queryObjectFlattened | 通过 |
| 接口文档契约 · bookGroupIsolated | 通过 |
| 接口文档契约 · responsesAndExampleDocumented | 通过 |
| 接口文档契约 · serversDocumented | 通过 |
| 接口文档契约 · opsGroupIsolated | 通过 |
| 接口文档契约 · categoryEnumInline | 通过 |
| 接口文档契约 · pathParameterDocumented | 通过 |

</details>

## 11-test

| 测试类（点击查看源码） | 用例数 | 失败 | 错误 | 跳过 |
| --- | ---: | ---: | ---: | ---: |
| OrderControllerTest | 9 | 0 | 0 | 0 |
| OrderIntegrationTest | 3 | 0 | 0 | 0 |
| OrderServiceTest | 16 | 0 | 0 | 0 |

<details>
<summary>展开逐项测试结果</summary>

| 用例 | 结果 |
| --- | --- |
| 订单接口（Web 层切片） · calculateBadRequest | 通过 |
| 订单接口（Web 层切片） · quickCalculateMissingParam | 通过 |
| 订单接口（Web 层切片） · calculateStockNotEnough | 通过 |
| 订单接口（Web 层切片） · calculateMissingField | 通过 |
| 订单接口（Web 层切片） · quickCalculate | 通过 |
| 订单接口（Web 层切片） · calculateOk | 通过 |
| 订单接口（Web 层切片） · hello | 通过 |
| 订单接口（Web 层切片） · calculateBrokenJson | 通过 |
| 订单接口（Web 层切片） · quickCalculateBadType | 通过 |
| 订单接口（集成测试） · calculateThroughRealStack | 通过 |
| 订单接口（集成测试） · quickCalculate | 通过 |
| 订单接口（集成测试） · stockNotEnoughThroughRealClient | 通过 |
| 边界与异常 · zeroAmount | 通过 |
| 边界与异常 · finalAmountMatrix(String, String)[1] | 通过 |
| 边界与异常 · finalAmountMatrix(String, String)[2] | 通过 |
| 边界与异常 · finalAmountMatrix(String, String)[3] | 通过 |
| 边界与异常 · finalAmountMatrix(String, String)[4] | 通过 |
| 边界与异常 · finalAmountMatrix(String, String)[5] | 通过 |
| 边界与异常 · finalAmountMatrix(String, String)[6] | 通过 |
| 边界与异常 · stockNotEnough | 通过 |
| 边界与异常 · stockQueriedOnce | 通过 |
| 边界与异常 · minimumAmount | 通过 |
| 满减规则 · noDiscountBelow100 | 通过 |
| 满减规则 · discountAt100 | 通过 |
| 满减规则 · discountAt300 | 通过 |
| 会员规则 · goldMemberExtraDiscount | 通过 |
| 会员规则 · memberLevelIgnoreCase | 通过 |
| 会员规则 · normalMemberNoExtra | 通过 |

</details>

## 12-actuator

| 测试类（点击查看源码） | 用例数 | 失败 | 错误 | 跳过 |
| --- | ---: | ---: | ---: | ---: |
| ActuatorEndpointTest | 7 | 0 | 0 | 0 |
| InventoryHealthIndicatorTest | 2 | 0 | 0 | 0 |
| AppInfoContributorTest | 2 | 0 | 0 | 0 |
| BusinessMetricsTest | 6 | 0 | 0 | 0 |

<details>
<summary>展开逐项测试结果</summary>

| 用例 | 结果 |
| --- | --- |
| 监控端点 · unexposedEndpointIs404 | 通过 |
| 监控端点 · healthHasInventoryComponent | 通过 |
| 监控端点 · counterGrowsByThree | 通过 |
| 监控端点 · metricListContainsBusinessMetrics | 通过 |
| 监控端点 · metricByName | 通过 |
| 监控端点 · prometheusTextFormat | 通过 |
| 监控端点 · infoHasCustomDetails | 通过 |
| 库存健康检查 · downWithReason | 通过 |
| 库存健康检查 · upWithDetails | 通过 |
| 应用信息 · contributesModuleAndOwner | 通过 |
| 应用信息 · startedAtMatchesJvmStartRatherThanRequestTime | 通过 |
| 业务指标 · timerRecordsDuration | 通过 |
| 业务指标 · arbitraryFailureReasonsShareOneBoundedLabel | 通过 |
| 业务指标 · failedCounterTaggedByReason | 通过 |
| 业务指标 · gaugeTracksProcessing | 通过 |
| 业务指标 · counterIncrements | 通过 |
| 业务指标 · timerSplitByResult | 通过 |

</details>

## 11-test 覆盖率

| 范围 | 指标 | 已覆盖 / 总数 | 覆盖率 |
| --- | --- | ---: | ---: |
| 整个 11-test 模块 | BRANCH | 16 / 18 | 88.9% |
| 整个 11-test 模块 | LINE | 69 / 73 | 94.5% |
| 整个 11-test 模块 | METHOD | 24 / 26 | 92.3% |
| OrderService | BRANCH | 11 / 12 | 91.7% |
| OrderService | LINE | 26 / 26 | 100.0% |
| OrderService | METHOD | 2 / 2 | 100.0% |

覆盖率反映执行到的代码范围，不能单独证明业务正确；上方测试源码展示具体断言。
