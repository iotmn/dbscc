package com.myclass.controller.student;

import com.myclass.entity.backstage.StudentInfo;
import com.myclass.service.backstage.StudentInfoService;
import com.myclass.tools.PageData;
import com.myclass.tools.TableParams;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;

/**
 * @author joe
 * @Date 2019/8/13
 */
@RestController("student/StudentInfoController")
@RequestMapping("student/studentInfo")
public class StudentInfoController {

    private Logger logger = Logger.getLogger(StudentInfoController.class);

    @Resource
    private StudentInfoService studentInfoService;

    @GetMapping("edit.html")
    public ModelAndView edit(ModelAndView modelAndView) {
        modelAndView.setViewName("student/edit");
        return modelAndView;
    }

    @GetMapping("detail/{stuNo}")
    public ModelAndView getStudentInfo(@PathVariable String stuNo, ModelAndView modelAndView) {
        modelAndView.setViewName("student/index");
        StudentInfo studentInfo = new StudentInfo();
        studentInfo.setStuNo(stuNo);
        studentInfo = studentInfoService.getStudentInfo(studentInfo);
        modelAndView.addObject("student", studentInfo);
        return modelAndView;
    }

    @GetMapping("classmate.html")
    public ModelAndView listStudentInfo(ModelAndView modelAndView) {
        modelAndView.setViewName("student/classmate");
        return modelAndView;
    }

    @PostMapping("classmate.json")
    public PageData<StudentInfo> listStudentInfo(@RequestBody TableParams<StudentInfo> tableParams, HttpServletRequest request) {
        StudentInfo student = (StudentInfo) request.getSession().getAttribute("student");
        StudentInfo studentInfoByClassID = new StudentInfo();
        studentInfoByClassID.setClassID(student.getClassID());
        tableParams.setData(studentInfoByClassID);
        PageData<StudentInfo> studentInfoPageData = studentInfoService.pageDataStudentInfo(tableParams.getData(), tableParams.getPageNumber(), tableParams.getPageSize(), tableParams.getSortName(), tableParams.getSortOrder());
        return studentInfoPageData;
    }

    @PostMapping("upload/head")
    public String upload(MultipartFile fileHead, HttpServletRequest request) {
        StudentInfo studentInfo = (StudentInfo) request.getSession().getAttribute("student");
        String newFileName = studentInfo.getStuNo() + "temp." + FilenameUtils.getExtension(fileHead.getOriginalFilename());
        File saveFile = new File(request.getSession().getServletContext().getRealPath("WEB-INF/statics/images/head"),
                newFileName);
        try {
            fileHead.transferTo(saveFile);
            boolean flag = studentInfoService.updateHeadTemp(studentInfo.getStuNo(), "statics/images/head/" + newFileName);
            if (!flag) {
                logger.error("????????????????????????");
                return "failed";
            }
            studentInfo.setIsHeadPass(0);
            studentInfo.setHeadTemp("statics/images/head/" + newFileName);
        } catch (IOException e) {
            logger.error("??????????????????:" + e);
        } catch (Exception e) {
            logger.error("error:" + e);
        }
        return "success";
    }

    @PostMapping("edit.do")
    public boolean edit(@RequestBody StudentInfo studentInfo, HttpServletRequest request) {
        if (studentInfoService.updateStudent(studentInfo)) {
            request.getSession().setAttribute("student", studentInfoService.getStudentInfo(studentInfo));
            return true;
        }
        return false;
    }

}
