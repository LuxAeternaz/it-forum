package luxaeterna.itforum.mapper;

import luxaeterna.itforum.entity.Upload;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UploadMapper {

    @Insert("INSERT INTO uploads (id, user_id, file_name, stored_name, file_path, file_size, mime_type, created_at) " +
            "VALUES (#{id}, #{userId}, #{fileName}, #{storedName}, #{filePath}, #{fileSize}, #{mimeType}, NOW())")
    int insert(Upload upload);

    @Select("SELECT * FROM uploads WHERE id = #{id}")
    Upload selectById(Long id);

    @Select("SELECT * FROM uploads WHERE stored_name = #{storedName}")
    Upload selectByStoredName(String storedName);
}
