package com.cpt202.projectselection.mapper;

import com.cpt202.projectselection.domain.ProjectApplicationLog;
import java.util.List;

public interface ProjectApplicationLogMapper {

    int insertLog(ProjectApplicationLog log);

    List<ProjectApplicationLog> selectLogsByApplicationId(Long applicationId);
}
