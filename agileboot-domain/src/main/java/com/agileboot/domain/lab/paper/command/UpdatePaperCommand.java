package com.agileboot.domain.lab.paper.command;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "更新论文命令")
public class UpdatePaperCommand extends CreatePaperCommand {
}
