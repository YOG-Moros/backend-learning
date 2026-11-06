"""Render selected Surefire and JaCoCo results as readable Markdown (no XML properties/logs)."""
import argparse
import html
import subprocess
import sys
from pathlib import Path
from urllib.parse import quote
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
MODULES = ("08-security", "10-doc", "11-test", "12-actuator")
REPOSITORY = "https://github.com/YOG-Moros/backend-learning"


def cell(value):
    return html.escape(str(value)).replace("|", "&#124;").replace("\n", " ")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--output", type=Path)
    parser.add_argument("--source-ref")
    parser.add_argument("--origin", choices=("local", "ci"), default="local")
    args = parser.parse_args()
    ref = args.source_ref or subprocess.check_output(
        ["git", "rev-parse", "HEAD"], cwd=ROOT, text=True).strip()
    source = f"{REPOSITORY}/blob/{quote(ref, safe='')}"
    origin = "本地构建" if args.origin == "local" else "本次 GitHub Actions 构建"
    lines = ["# 四份作业测试结果", "", f"来源：{origin}产生的 Surefire XML 与 JaCoCo XML。",
             f"对应源码：[{cell(ref[:12])}]({REPOSITORY}/commit/{quote(ref, safe='')})。",
             "仅摘录测试名称、结果与覆盖率，不公开原始 XML 中的环境变量和运行日志。",
             "这是构建结果摘要；生成摘要不会重新执行测试。", "",
             "| 作业 | 测试数 | 失败 | 错误 | 跳过 | 状态 |", "| --- | ---: | ---: | ---: | ---: | --- |"]
    reports = {}
    for module in MODULES:
        suites = [ET.parse(path).getroot() for path in sorted((ROOT / module / "target/surefire-reports").glob("TEST-*.xml"))]
        reports[module] = suites
        totals = [sum(int(suite.get(key, "0")) for suite in suites) for key in ("tests", "failures", "errors", "skipped")]
        state = "未生成报告" if not suites else ("存在失败" if totals[1] or totals[2] else ("含跳过项" if totals[3] else "通过"))
        lines.append(f"| {module} | {' | '.join(map(str, totals))} | {state} |")
    for module, suites in reports.items():
        lines += ["", f"## {module}", "", "| 测试类（点击查看源码） | 用例数 | 失败 | 错误 | 跳过 |", "| --- | ---: | ---: | ---: | ---: |"]
        for suite in suites:
            cases = list(suite.iter("testcase"))
            name = cases[0].get("classname", suite.get("name", "")) if cases else suite.get("name", "")
            outer = name.split("$")[0]
            relative = f"{module}/src/test/java/{outer.replace('.', '/')}.java"
            label = cell(suite.get("name", name).split(".")[-1])
            link = f"[{label}]({source}/{quote(relative)})" if (ROOT / relative).is_file() else label
            counts = " | ".join(suite.get(key, "0") for key in ("tests", "failures", "errors", "skipped"))
            lines.append(f"| {link} | {counts} |")
        lines += ["", "<details>", "<summary>展开逐项测试结果</summary>", "", "| 用例 | 结果 |", "| --- | --- |"]
        for suite in suites:
            for case in suite.iter("testcase"):
                state = "错误" if case.find("error") is not None else "失败" if case.find("failure") is not None else "跳过" if case.find("skipped") is not None else "通过"
                lines.append(f"| {cell(case.get('classname', '').split('.')[-1])} · {cell(case.get('name', ''))} | {state} |")
        lines += ["", "</details>"]
    coverage = ROOT / "11-test/target/site/jacoco/jacoco.xml"
    lines += ["", "## 11-test 覆盖率", ""]
    if coverage.exists():
        root = ET.parse(coverage).getroot()
        lines += ["| 范围 | 指标 | 已覆盖 / 总数 | 覆盖率 |", "| --- | --- | ---: | ---: |"]
        ranges = [("整个 11-test 模块", root)]
        ranges += [("OrderService", c) for c in root.findall("package/class") if c.get("name", "").endswith("/OrderService")]
        for label, node in ranges:
            for counter in node.findall("counter"):
                if counter.get("type") not in ("LINE", "BRANCH", "METHOD"):
                    continue
                covered, missed = int(counter.get("covered")), int(counter.get("missed"))
                total = covered + missed
                percentage = f"{covered / total:.1%}" if total else "不适用"
                lines.append(f"| {label} | {counter.get('type')} | {covered} / {total} | {percentage} |")
        lines += ["", "覆盖率反映执行到的代码范围，不能单独证明业务正确；上方测试源码展示具体断言。"]
    else:
        lines.append("本次未生成 JaCoCo 报告，不提供覆盖率数字。")
    result = "\n".join(lines) + "\n"
    if args.output:
        args.output.parent.mkdir(parents=True, exist_ok=True)
        args.output.write_text(result, encoding="utf-8")
    else:
        sys.stdout.reconfigure(encoding="utf-8")
        sys.stdout.write(result)


if __name__ == "__main__":
    main()
