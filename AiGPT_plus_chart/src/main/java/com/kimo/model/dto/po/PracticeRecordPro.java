package com.kimo.model.dto.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * @author Mr.kimo
 */
@TableName(value ="practice_record_pro")
@Data
public class PracticeRecordPro implements Serializable {
    /**
     * id
     */
    @TableId
    private Long id;

    /**
     * 课程id
     */
    private Long courseId;

    /**
     * 用户id
     */
    private Long userId;

    /**
     *  (答对+1)   默认为0
     */
    private Integer userRight;

    /**
     *  (答错+1)   默认为0
     */
    private Integer userError;

    /**
     * 未考完，考完
     */
    private String practiceStatus;

    /**
     * (ai的评估评价)
     */
    private String airesult;

    /**
     * （科目）
     */
    private String subjects;

    /**
     * （记录练习的分数）
     */
    private Double practicePoint;

    /**
     * 考试记录表的创建的时间
     */
    private Date createTime;

    /**
     * 学生练习总时间
     */
    private Date endTime;

    /**
     * 学历
     */
    private String qualifications;

    /**
     * 章节id
     */
    private Long teachplanId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        PracticeRecordPro other = (PracticeRecordPro) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getCourseId() == null ? other.getCourseId() == null : this.getCourseId().equals(other.getCourseId()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getUserRight() == null ? other.getUserRight() == null : this.getUserRight().equals(other.getUserRight()))
            && (this.getUserError() == null ? other.getUserError() == null : this.getUserError().equals(other.getUserError()))
            && (this.getPracticeStatus() == null ? other.getPracticeStatus() == null : this.getPracticeStatus().equals(other.getPracticeStatus()))
            && (this.getAiresult() == null ? other.getAiresult() == null : this.getAiresult().equals(other.getAiresult()))
            && (this.getSubjects() == null ? other.getSubjects() == null : this.getSubjects().equals(other.getSubjects()))
            && (this.getPracticePoint() == null ? other.getPracticePoint() == null : this.getPracticePoint().equals(other.getPracticePoint()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getEndTime() == null ? other.getEndTime() == null : this.getEndTime().equals(other.getEndTime()))
            && (this.getQualifications() == null ? other.getQualifications() == null : this.getQualifications().equals(other.getQualifications()))
            && (this.getTeachplanId() == null ? other.getTeachplanId() == null : this.getTeachplanId().equals(other.getTeachplanId()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getCourseId() == null) ? 0 : getCourseId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getUserRight() == null) ? 0 : getUserRight().hashCode());
        result = prime * result + ((getUserError() == null) ? 0 : getUserError().hashCode());
        result = prime * result + ((getPracticeStatus() == null) ? 0 : getPracticeStatus().hashCode());
        result = prime * result + ((getAiresult() == null) ? 0 : getAiresult().hashCode());
        result = prime * result + ((getSubjects() == null) ? 0 : getSubjects().hashCode());
        result = prime * result + ((getPracticePoint() == null) ? 0 : getPracticePoint().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getEndTime() == null) ? 0 : getEndTime().hashCode());
        result = prime * result + ((getQualifications() == null) ? 0 : getQualifications().hashCode());
        result = prime * result + ((getTeachplanId() == null) ? 0 : getTeachplanId().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", courseId=").append(courseId);
        sb.append(", userId=").append(userId);
        sb.append(", userRight=").append(userRight);
        sb.append(", userError=").append(userError);
        sb.append(", practiceStatus=").append(practiceStatus);
        sb.append(", airesult=").append(airesult);
        sb.append(", subjects=").append(subjects);
        sb.append(", practicePoint=").append(practicePoint);
        sb.append(", createTime=").append(createTime);
        sb.append(", endTime=").append(endTime);
        sb.append(", qualifications=").append(qualifications);
        sb.append(", teachplanId=").append(teachplanId);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}