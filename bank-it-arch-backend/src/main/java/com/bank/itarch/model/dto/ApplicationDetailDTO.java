package com.bank.itarch.model.dto;

import com.bank.itarch.model.entity.ArchApplication;
import com.bank.itarch.model.entity.ArchApplicationModule;
import com.bank.itarch.model.entity.ArchDependency;
import com.bank.itarch.model.entity.ArchService;
import lombok.Data;

import java.util.List;

@Data
public class ApplicationDetailDTO {
    private ArchApplication application;
    private List<ArchApplicationModule> modules;
    private List<ArchService> services;
    private List<ArchDependency> dependencies;
}
