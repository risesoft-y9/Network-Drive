package net.risesoft.service.Impl;

import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import cn.hutool.core.util.NumberUtil;
import lombok.RequiredArgsConstructor;

import net.risesoft.entity.CategoryTable;
import net.risesoft.entity.DataApiOnlineEntity;
import net.risesoft.entity.DataAssets;
import net.risesoft.entity.DataAssetsNumberRules;
import net.risesoft.entity.DataSourceEntity;
import net.risesoft.entity.DictionaryValue;
import net.risesoft.entity.FileInfo;
import net.risesoft.entity.MetadataConfig;
import net.risesoft.enums.CategoryEnums;
import net.risesoft.enums.CodePayTypeEnum;
import net.risesoft.enums.CodeTypeEnum;
import net.risesoft.enums.MarginTypeEnum;
import net.risesoft.interfaces.Four;
import net.risesoft.interfaces.Six;
import net.risesoft.model.AssetsModel;
import net.risesoft.model.CodePicBase64;
import net.risesoft.model.IdcodeRegResult;
import net.risesoft.model.SearchPage;
import net.risesoft.model.platform.DataCatalog;
import net.risesoft.pojo.Y9Page;
import net.risesoft.pojo.Y9Result;
import net.risesoft.repository.AudioFileRepository;
import net.risesoft.repository.DataApiOnlineRepository;
import net.risesoft.repository.DataAssetsNumberRulesRepository;
import net.risesoft.repository.DataAssetsRepository;
import net.risesoft.repository.DocumentFileRepository;
import net.risesoft.repository.FileInfoRepository;
import net.risesoft.repository.ImageFileRepository;
import net.risesoft.repository.MetadataConfigRepository;
import net.risesoft.repository.VideoFileRepository;
import net.risesoft.repository.spec.DataAssetsSpecification;
import net.risesoft.service.CategoryTableService;
import net.risesoft.service.DataAssetsService;
import net.risesoft.service.DataSourceService;
import net.risesoft.service.DictionaryOptionService;
import net.risesoft.util.Config;
import net.risesoft.util.DataConstant;
import net.risesoft.util.FileDataUtil;
import net.risesoft.util.PageUtil;
import net.risesoft.y9.Y9Context;
import net.risesoft.y9.Y9LoginUserHolder;
import net.risesoft.y9.util.Y9BeanUtil;
import net.risesoft.y9public.entity.Y9FileStore;
import net.risesoft.y9public.service.Y9FileStoreService;
import y9.client.rest.platform.permission.PersonRoleApiClient;
import y9.client.rest.platform.resource.DataCatalogApiClient;

@Service
@RequiredArgsConstructor
public class DataAssetsServiceImpl implements DataAssetsService {

    private final DataAssetsRepository dataAssetsRepository;

    private final DataCatalogApiClient dataCatalogApiClient;
    private final DocumentFileRepository documentFileRepository;
    private final AudioFileRepository audioFileRepository;
    private final ImageFileRepository imageFileRepository;
    private final VideoFileRepository videoFileRepository;
    private final CategoryTableService categoryTableService;
    private final PageUtil pageUtil;
    private final MetadataConfigRepository metadataConfigRepository;
    private final DataAssetsNumberRulesRepository dataAssetsNumberRulesRepository;
    private final FileInfoRepository fileInfoRepository;
    private final Y9FileStoreService y9FileStoreService;
    private final DataApiOnlineRepository dataApiOnlineRepository;
    private final DataSourceService dataSourceService;
    private final PersonRoleApiClient personRoleApiClient;
    private final DictionaryOptionService dictionaryOptionService;

    public static Object getFieldValue(Object entity, String fieldName) {
        try {
            Field field = entity.getClass().getDeclaredField(fieldName); // 获取字段
            field.setAccessible(true); // 设置字段可访问
            return field.get(entity); // 获取字段值
        } catch (NoSuchFieldException e) {
            e.printStackTrace(); // 字段不存在
        } catch (IllegalAccessException e) {
            e.printStackTrace(); // 访问字段时异常
        }
        return null; // 返回null表示未找到或无法访问
    }

    @Override
    public SearchPage<DataAssets> list(String categoryId, Boolean isDeleted, int page, int rows) {
        Pageable pageable = PageRequest.of(page - 1, rows, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<DataAssets> pageList = dataAssetsRepository.findByCategoryIdAndIsDeleted(categoryId, isDeleted, pageable);
        List<DataAssets> list = pageList.getContent();
        SearchPage<DataAssets> searchPage = SearchPage.<DataAssets>builder().rows(list).currpage(page).size(rows)
            .totalpages(pageList.getTotalPages()).total(pageList.getTotalElements()).build();
        return searchPage;
    }

    @Override
    public SearchPage<DataAssets> listByColumnNameAndValues(String categoryId, Boolean isDeleted,
        String columnNameAndValues, int page, int rows) {
        String tenantId = Y9LoginUserHolder.getTenantId();
        DataCatalog dataCatalog = dataCatalogApiClient.getTreeRoot(tenantId, categoryId).getData();
        String customId = dataCatalog.getCustomId();
        String[] arr = columnNameAndValues.split(";");
        String joinSql = "";
        String conditionSql = "";
        for (int i = 0; i < arr.length; i++) {
            String[] arrTemp = arr[i].split(":");
            if (arrTemp.length == 2) {
                String columnNameStr = arrTemp[0];
                String value = arrTemp[1];
                MetadataConfig metadataConfig =
                    metadataConfigRepository.findByViewTypeAndColumnName(customId, columnNameStr);
                String sign = "";
                if (null != metadataConfig) {
                    if (metadataConfig.getFieldOrigin().equals("baseInfo")) {
                        sign = "T";
                    } else {
                        sign = "C";
                    }
                }
                String columnName = arrTemp[0].toUpperCase();
                if (StringUtils.isBlank(conditionSql)) {
                    conditionSql += "INSTR(" + sign + "." + columnName + ",'" + value + "') > 0 ";
                } else {
                    conditionSql += " AND INSTR(" + sign + "." + columnName + ",'" + value + "') > 0 ";
                }
            }
        }
        if (customId.equals(CategoryEnums.DOCUMENT.getEnName())) {
            joinSql = "LEFT JOIN Y9_DATAASSETS_DOCUMENT_FILE C ON T.DATAASSETS_ID = C.DETAIL_ID";
        } else if (customId.equals(CategoryEnums.IMAGE.getEnName())) {
            joinSql = "LEFT JOIN Y9_DATAASSETS_IMAGE_FILE C ON T.DATAASSETS_ID = C.DETAIL_ID";
        } else if (customId.equals(CategoryEnums.AUDIO.getEnName())) {
            joinSql = "LEFT JOIN Y9_DATAASSETS_AUDIO_FILE C ON T.DATAASSETS_ID = C.DETAIL_ID";
        } else if (customId.equals(CategoryEnums.VIDEO.getEnName())) {
            joinSql = "LEFT JOIN Y9_DATAASSETS_VIDEO_FILE C ON T.DATAASSETS_ID = C.DETAIL_ID";
        } else {
            CategoryTable categoryTable = categoryTableService.findByCategoryMark(customId);
            if (null != categoryTable) {
                joinSql = "LEFT JOIN " + categoryTable.getTableName() + " C ON T.DATAASSETS_ID = C.DETAIL_ID";
            }
        }
        String sql = "SELECT T.* FROM Y9_DATAASSETS_DETAILS T " + joinSql
            + " WHERE T.CATEGORY_ID = ? AND T.IS_DELETED = ? AND " + conditionSql + " ORDER BY T.CREATE_TIME DESC";
        String countSql = "SELECT COUNT(T.DATAASSETS_ID) FROM Y9_DATAASSETS_DETAILS T " + joinSql
            + " WHERE T.CATEGORY_ID = ? AND T.IS_DELETED = ? AND " + conditionSql;
        System.out.println(sql);
        System.out.println(countSql);
        Object[] args = new Object[2];
        args[0] = categoryId;
        args[1] = isDeleted;
        SearchPage<DataAssets> searchPage =
            pageUtil.page(sql, args, new BeanPropertyRowMapper<>(DataAssets.class), countSql, args, page, rows);
        return searchPage;
    }

    @Override
    public DataAssets save(DataAssets dataAssets) {
        return dataAssetsRepository.save(dataAssets);
    }

    @Override
    public DataAssets findById(Long id) {
        return dataAssetsRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional(readOnly = false)
    public void delete(String categoryId, Long[] ids) {
        String tenantId = Y9LoginUserHolder.getTenantId();
        DataCatalog dataCatalog = dataCatalogApiClient.getTreeRoot(tenantId, categoryId).getData();
        for (Long id : ids) {
            dataAssetsRepository.deleteById(id);
            if (dataCatalog.getCustomId().equals(CategoryEnums.DOCUMENT.getEnName())) {
                documentFileRepository.deleteByDetailId(id);
            } else if (dataCatalog.getCustomId().equals(CategoryEnums.IMAGE.getEnName())) {
                imageFileRepository.deleteByDetailId(id);
            } else if (dataCatalog.getCustomId().equals(CategoryEnums.AUDIO.getEnName())) {
                audioFileRepository.deleteByDetailId(id);
            } else if (dataCatalog.getCustomId().equals(CategoryEnums.VIDEO.getEnName())) {
                videoFileRepository.deleteByDetailId(id);
            } else {
                categoryTableService.deleteTableData(dataCatalog.getCustomId(), id.toString());
            }
        }
    }

    @Override
    @Transactional(readOnly = false)
    public void signDelete(String categoryId, Long[] ids) {
        for (Long id : ids) {
            DataAssets dataAssets = this.findById(id);
            if (null != dataAssets) {
                dataAssets.setIsDeleted(true);
                this.save(dataAssets);
            }
        }
    }

    @Override
    @Transactional(readOnly = false)
    public void recordArchiving(Long[] ids) {
        for (Long id : ids) {
            DataAssets dataAssets = this.findById(id);
            if (null != dataAssets) {
                dataAssets.setStatus(1);
                this.save(dataAssets);
            }
        }
    }

    @Override
    @Transactional(readOnly = false)
    public Y9Result<String> createAssetsNo(String categoryId, Long[] ids) {
        String tenantId = Y9LoginUserHolder.getTenantId();
        DataCatalog dataCatalog = dataCatalogApiClient.getTreeRoot(tenantId, categoryId).getData();
        for (Long id : ids) {
            DataAssets dataAssets = this.findById(id);
            List<DataAssetsNumberRules> rulesList =
                dataAssetsNumberRulesRepository.findByCategoryMark(dataCatalog.getCustomId());
            if (null != rulesList && rulesList.size() > 0) {
                String archiveNo = "";
                for (DataAssetsNumberRules rules : rulesList) {
                    // TODO 根据规则生成资产号
                    Object fieldValue = getFieldValue(dataAssets, rules.getFieldName()).toString();
                    if (null != fieldValue) {
                        if (StringUtils.isNotBlank(archiveNo) && StringUtils.isNotBlank(rules.getConnectorSymbol())) {
                            archiveNo += rules.getConnectorSymbol() + fieldValue;
                        } else {
                            archiveNo += fieldValue.toString();
                        }
                    }
                }
                System.out.println("资产编号：" + archiveNo);
                dataAssets.setCode(archiveNo);
            } else {
                return Y9Result.failure("未找到资产号规则");
            }
            this.save(dataAssets);
        }
        return Y9Result.success("创建资产号成功");
    }

    @Override
    public List<DataAssets> findByDataAssetsIdIn(Long[] ids) {
        return dataAssetsRepository.findByIdIn(ids);
    }

	@Override
	@Transactional(readOnly = false)
	public Y9Result<String> saveDataAssets(DataAssets dataAssets) {
		try {
			dataAssets.setTenantId(Y9LoginUserHolder.getTenantId());
			// 计算排序号
			if(dataAssets.getOrderNum() == null) {
				Integer orderNum = dataAssetsRepository.getMaxOrderNum(dataAssets.getCategoryId());
				if(orderNum == null) {
					orderNum = 1;
				}else {
					orderNum = orderNum + 1;
				}
				dataAssets.setOrderNum(orderNum);
			}
			if(dataAssets.getId() == null) {
				dataAssets.setCreator(Y9LoginUserHolder.getUserInfo().getName());
				dataAssets.setCreatorId(Y9LoginUserHolder.getPersonId());
				dataAssets.setDataState("out");
				dataAssetsRepository.save(dataAssets);
				return Y9Result.successMsg("新增成功");
			}else {
				DataAssets dataAssets2 = findById(dataAssets.getId());
				Y9BeanUtil.copyProperties(dataAssets, dataAssets2);
				dataAssets2.setUpdateUser(Y9LoginUserHolder.getUserInfo().getName());
				dataAssets2.setUpdateUserId(Y9LoginUserHolder.getPersonId());
				dataAssetsRepository.save(dataAssets2);
				return Y9Result.successMsg("修改成功");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return Y9Result.failure("保存失败：" + e.getMessage());
		}
	}

	@Override
	public Page<DataAssets> searchPage(String categoryId, String name, String code, Integer status, String dataState, int page, int size) {
		if (page < 0) {
            page = 1;
        }
		// 判断是否系统管理员
		boolean isAdmin = personRoleApiClient.hasRole(Y9LoginUserHolder.getTenantId(), "dataAssets", null, "系统管理员", Y9LoginUserHolder.getPersonId()).getData();
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.ASC, "createTime"));
        DataAssetsSpecification spec = new DataAssetsSpecification(categoryId, name, Y9LoginUserHolder.getTenantId(), code, false, status, 
        		dataState, isAdmin ? "" : Y9LoginUserHolder.getPersonId(), "", "", "", "");
		return dataAssetsRepository.findAll(spec, pageable);
	}

	@Override
	public Y9Result<String> deleteDataAssets(Long id) {
		DataAssets dataAssets = findById(id);
		if(dataAssets != null) {
			dataAssets.setIsDeleted(true);
			dataAssets.setDeleteUser(Y9LoginUserHolder.getUserInfo().getName());
			dataAssets.setDeleteUserId(Y9LoginUserHolder.getPersonId());
			dataAssetsRepository.save(dataAssets);
		}else {
			return Y9Result.failure("数据不存在，请刷新数据");
		}
		return Y9Result.successMsg("删除成功");
	}

	@Override
	@Transactional(readOnly = false)
	public Y9Result<String> fileUpload(MultipartFile file, Long assetsId) {
		String identifier = "", fileName = "";
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
			fileName = file.getOriginalFilename();// 文件名称
			String fileType = fileName.substring(fileName.lastIndexOf(".") + 1);// 文件类型
			if(fileType.indexOf("js") > -1 || fileType.indexOf("htm") > -1 || fileType.indexOf("jar") > -1) {
				return Y9Result.failure("不支持上传的文件格式：" + fileName);
			}else {
				// 文件存储地址
				String savePath = Y9Context.getSystemName() + "/" + sdf.format(new Date()) + "/";
				// 保存文件
				Y9FileStore y9FileStore = y9FileStoreService.uploadFile(file, savePath, fileName);
				if(y9FileStore != null) {
					// 获取文件名称，不带后缀
					identifier = fileName.substring(0, fileName.lastIndexOf("."));
					FileInfo info = new FileInfo();
					info.setFileSize(FileDataUtil.getFileSize(file.getSize()));
					info.setName(fileName);
					info.setFilePath(y9FileStore.getId());
					info.setAssetsId(assetsId);
					info.setIdentifier(identifier);
					info.setFileType("文件");
					fileInfoRepository.save(info);
					
					DataAssets dataAssets = findById(assetsId);
					if(dataAssets != null) {
						dataAssets.setMountType("文件");
						dataAssetsRepository.save(dataAssets);
					}
				}else {
					return Y9Result.failure("上传失败：" + fileName + "，请重新上传");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return Y9Result.failure(fileName + "-上传失败：" + e.getMessage());
		}
		return Y9Result.successMsg("上传成功：" + identifier);
	}

	@Override
	@Transactional(readOnly = false)
	public Y9Result<String> updownData(Long id) {
		try {
			DataAssets dataAssets = findById(id);
			if(dataAssets != null) {
				dataAssets.setStatus(dataAssets.getStatus() == 0 ? 1 : 0);
				dataAssetsRepository.save(dataAssets);
			}else {
				return Y9Result.failure("保存失败，数据不存在");
			}
			return Y9Result.successMsg("保存成功");
		} catch (Exception e) {
			e.printStackTrace();
			return Y9Result.failure("保存失败：" + e.getMessage());
		}
	}

	@Override
	@Transactional(readOnly = false)
	public Y9Result<String> genQr(Long id) {
		try {
			DataAssets dataAssets = findById(id);
			if(dataAssets != null) {
				dataAssets.setCodeGlobal(getIdCode(id, dataAssets.getName()));
				dataAssets.setQrcode(getIdCodeQr(dataAssets.getCodeGlobal()));
				dataAssetsRepository.save(dataAssets);
			}
			return Y9Result.successMsg("赋码成功");
		} catch (Exception e) {
			e.printStackTrace();
			return Y9Result.failure("赋码失败：" + e.getMessage());
		}
	}
	
	/**
     * 获取全球统一码
     *
     * @param id
     * @param name
     * @return
     */
    private String getIdCode(Long id, String name) {
        String _id = "dataassets-" + id;
        IdcodeRegResult result = Four.m407(Config.MAIN_CODE, "60", 10167, "11000000", _id, "", name, CodePayTypeEnum.REGISTER.getValue(), 
        		Config.GOTO_URL + "?tenantId=" + Y9LoginUserHolder.getTenantId(), Config.SAMPLE_URL + "?tenantId=" + Y9LoginUserHolder.getTenantId());
        return result.getOrganUnitIdCode();
    }

    /**
     * 获取全球统一码二维码
     *
     * @param code
     * @return
     */
    private String getIdCodeQr(String code) {
        CodePicBase64 result = Six.m605(Config.MAIN_CODE, code, 1, "", MarginTypeEnum.SQUARE.getValue(), 1, 1,
        		CodeTypeEnum.QR.getValue(), 5, "000000");
        return "data:image/;base64," + result.getStr();
    }

	@Override
	public Y9Result<String> genCode(String categoryId, String pCode) {
        String maxCode = dataAssetsRepository.getMaxCode(categoryId);
        String code = "";
        if (StringUtils.isBlank(maxCode)) {
            if (StringUtils.isNotBlank(pCode)) {
                code = pCode + "-" + String.format("%0" + "4d", 1);
            } else {
                code = String.format("%0" + "4d", 1);
            }
        } else {
            code = maxCode;
            String last = "";
            int len = 0;
            if (code.length() > 1) {
                Pattern p = Pattern.compile("\\d{2,}");// 这个2是指连续数字的最少个数
                Matcher m = p.matcher(code);
                while (m.find()) {
                    last = m.group();
                    len = last.length();
                }
            } else {
                last = code;
                len = 1;
            }
            if (code.length() > 1) {
                code = code.substring(0, code.length() - last.length())
                        + String.format("%0" + len + "d", NumberUtil.parseInt(last) + 1);
            } else {
                code = String.format("%0" + len + "d", NumberUtil.parseInt(last) + 1);
            }
        }
        return Y9Result.success(code);
	}

	@Override
	public Page<FileInfo> getFilePage(Long id, String name, int page, int size) {
		if (page < 0) {
            page = 1;
        }
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.ASC, "createTime"));
        Page<FileInfo> filePage = null;
        if(StringUtils.isBlank(name)) {
        	filePage = fileInfoRepository.findByAssetsId(id, pageable);
        }else {
        	filePage = fileInfoRepository.findByAssetsIdAndNameContains(id, name, pageable);
        }
		return filePage;
	}

	@Override
	public void fileDownload(Long id, HttpServletResponse response, HttpServletRequest request) {
		try {
			FileInfo file = fileInfoRepository.findById(id).orElse(null);
            String filename = file.getName();
            String filePath = file.getFilePath();
            if (request.getHeader("User-Agent").toLowerCase().indexOf("firefox") > 0) {
                filename = new String(filename.getBytes(StandardCharsets.UTF_8), "ISO8859-1");// 火狐浏览器
            } else {
                filename = URLEncoder.encode(filename, StandardCharsets.UTF_8);
            }
            OutputStream out = response.getOutputStream();
            response.reset();
            response.setHeader("Content-disposition", "attachment; filename=\"" + filename + "\"");
            response.setHeader("Content-type", "text/html;charset=UTF-8");
            response.setContentType("application/octet-stream");
            y9FileStoreService.downloadFileToOutputStream(filePath, out);
            out.flush();
            out.close();
        } catch (Exception e) {
        	e.printStackTrace();
        }
	}

	@Override
	public Y9Result<DataAssets> uploadPicture(MultipartFile file, Long assetsId) {
		DataAssets dataAssets = null;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
			String fileName = file.getOriginalFilename();// 文件名称
			// 文件存储地址
			String savePath = Y9Context.getSystemName() + "/" + sdf.format(new Date()) + "/";
			// 保存文件
			Y9FileStore y9FileStore = y9FileStoreService.uploadFile(file, savePath, fileName);
			if(y9FileStore != null) {
				if(assetsId == 0) {
					dataAssets = new DataAssets();
					dataAssets.setTenantId(Y9LoginUserHolder.getTenantId());
					dataAssets.setCreator(Y9LoginUserHolder.getUserInfo().getName());
					dataAssets.setCreatorId(Y9LoginUserHolder.getPersonId());
					dataAssets.setDataState("out");
				}else {
					dataAssets = findById(assetsId);
				}
				String fileUrl = y9FileStore.getUrl();
				if(!fileUrl.startsWith("http")) {
					fileUrl = Y9Context.getProperty("y9.common.dataAssetsBaseUrl") + fileUrl;
				}
				dataAssets.setPicture(fileUrl);
				dataAssetsRepository.save(dataAssets);
			}else {
				return Y9Result.failure("设置失败");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return Y9Result.failure("设置失败：" + e.getMessage());
		}
		return Y9Result.success(dataAssets, "设置成功");
	}

	@Override
	@Transactional(readOnly = false)
	public Y9Result<String> saveAssetsInterface(String ids, Long assetsId) {
		try {
			String[] interfaceIds = ids.split(",");
			for(String id : interfaceIds) {
				DataApiOnlineEntity dataApiOnlineEntity = dataApiOnlineRepository.findById(id).orElse(null);
				FileInfo info = new FileInfo();
				info.setName(dataApiOnlineEntity.getName());
				info.setAssetsId(assetsId);
				info.setIdentifier(dataApiOnlineEntity.getId());
				info.setFileType("接口");
				fileInfoRepository.save(info);
			}
			DataAssets dataAssets = findById(assetsId);
			if(dataAssets != null) {
				dataAssets.setMountType("接口");
				dataAssetsRepository.save(dataAssets);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return Y9Result.failure("保存失败：" + e.getMessage());
		}
		return Y9Result.successMsg("保存成功");
	}

	@Override
	@Transactional(readOnly = false)
	public Y9Result<String> saveAssetsTable(String ids, Long assetsId) {
		try {
			String[] interfaceIds = ids.split(",");
			for(String id : interfaceIds) {
				if(id.startsWith("s-")) {// 数据库
					DataSourceEntity dataSourceEntity = dataSourceService.getDataSourceById(id.split("-")[1]);
					FileInfo info = new FileInfo();
					info.setName(dataSourceEntity.getName());
					info.setAssetsId(assetsId);
					info.setIdentifier(dataSourceEntity.getId());
					info.setFileType("数据库");
					fileInfoRepository.save(info);
				}else if(id.startsWith("t-")) {// 数据表
					String[] obj = id.split("-");
					DataSourceEntity dataSourceEntity = dataSourceService.getDataSourceById(obj[1]);
					FileInfo info = new FileInfo();
					info.setName(obj[2]);
					info.setAssetsId(assetsId);
					info.setIdentifier(dataSourceEntity.getId());
					info.setFileType("数据表");
					fileInfoRepository.save(info);
				}
			}
			DataAssets dataAssets = findById(assetsId);
			if(dataAssets != null) {
				dataAssets.setMountType("数据");
				dataAssetsRepository.save(dataAssets);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return Y9Result.failure("保存失败：" + e.getMessage());
		}
		return Y9Result.successMsg("保存成功");
	}

	@Override
	@Transactional(readOnly = false)
	public Y9Result<String> examineData(Long id, String dataState) {
		try {
			DataAssets dataAssets = findById(id);
			if(dataAssets != null) {
				dataAssets.setDataState(dataState);
				dataAssetsRepository.save(dataAssets);
			}else {
				return Y9Result.failure("保存失败，数据不存在");
			}
			return Y9Result.successMsg("保存成功");
		} catch (Exception e) {
			e.printStackTrace();
			return Y9Result.failure("保存失败：" + e.getMessage());
		}
	}

	@Override
	@Transactional(readOnly = false)
	public Y9Result<String> deleteMountData(Long id) {
		try {
			FileInfo info = fileInfoRepository.findById(id).orElse(null);
			if(info != null && info.getFileType().equals("文件")) {
				y9FileStoreService.deleteFile(info.getFilePath());
			}
			fileInfoRepository.deleteById(id);
			// 判断挂接数据量，为零时取出资产上的挂接状态
			long count = fileInfoRepository.countByAssetsId(info.getAssetsId());
			if(count == 0) {
				DataAssets dataAssets = findById(info.getAssetsId());
				if(dataAssets != null) {
					dataAssets.setMountType("");
					dataAssetsRepository.save(dataAssets);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return Y9Result.failure("删除失败：" + e.getMessage());
		}
		return Y9Result.successMsg("删除成功");
	}

	@Override
	public Y9Page<Map<String, Object>> searchPage2(String searchText, String appScenarios, String dataZone, 
			String productType, Integer sort, int page, int size) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		List<Map<String, Object>> listMap = new ArrayList<Map<String,Object>>();
		Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.ASC, "orderNum"));
		if(sort == null) {
			pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.ASC, "orderNum"));
		}else if(sort == 1) {
			pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));
		}else if(sort == 2) {
			pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "clickNum"));
		}
        DataAssetsSpecification spec = new DataAssetsSpecification("", "", Y9LoginUserHolder.getTenantId(), "", false, 1, "in", "", searchText, 
        		appScenarios, dataZone, productType);
		Page<DataAssets> assetsPage = dataAssetsRepository.findAll(spec, pageable);
		for(DataAssets dataAssets : assetsPage) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("id", dataAssets.getId());
			map.put("title", dataAssets.getName());
			map.put("unit", dataAssets.getDataProvider());
			map.put("description", dataAssets.getRemark());
			map.put("src", dataAssets.getPicture());
			map.put("date", sdf.format(dataAssets.getCreateTime()));
			map.put("tag", DataConstant.getShareType(dataAssets.getShareType()));
			listMap.add(map);
		}
		return Y9Page.success(page, assetsPage.getTotalPages(), assetsPage.getTotalElements(), listMap, "获取数据成功");
	}

	@Override
	public Y9Result<AssetsModel> getDataById(Long id) {
		AssetsModel model = new AssetsModel();
		DataAssets dataAssets = findById(id);
		if(dataAssets != null) {
			Y9BeanUtil.copyProperties(dataAssets, model);
			model.setShareTypeName(DataConstant.getShareType(dataAssets.getShareType()));
			model.setDataType(dictionaryOptionService.findByCodeAndType(dataAssets.getDataType(), "assetsType"));
			model.setAppScenarios(dictionaryOptionService.findByCodeAndType(dataAssets.getAppScenarios(), "appScenarios"));
			model.setDataZone(dictionaryOptionService.findByCodeAndType(dataAssets.getDataZone(), "dataZone"));
			model.setProductType(dictionaryOptionService.findByCodeAndType(dataAssets.getProductType(), "productType"));
			
			// 保存点击次数
			dataAssets.setClickNum(dataAssets.getClickNum() + 1);
			dataAssetsRepository.save(dataAssets);
		}
		return Y9Result.success(model);
	}

	@Override
	public Y9Result<AssetsModel> getDataByQrCode(String qrcode) {
		AssetsModel model = new AssetsModel();
		DataAssets dataAssets = dataAssetsRepository.findByCodeGlobalAndIsDeleted(qrcode, false);
		if(dataAssets != null) {
			Y9BeanUtil.copyProperties(dataAssets, model);
			model.setShareTypeName(DataConstant.getShareType(dataAssets.getShareType()));
			model.setDataType(dictionaryOptionService.findByCodeAndType(dataAssets.getDataType(), "assetsType"));
		}
		return Y9Result.success(model);
	}

	@Override
	public Y9Result<List<Map<String, Object>>> getDataFilter() {
		List<Map<String, Object>> listMap = new ArrayList<Map<String,Object>>();
		// 应用场景
		Map<String, Object> map1 = new HashMap<String, Object>();
		map1.put("title", "应用场景");
		map1.put("key", "appScenarios");
		
		List<Map<String, Object>> options1 = new ArrayList<Map<String,Object>>();
		Map<String, Object> dataMap1 = new HashMap<String, Object>();
		dataMap1.put("label", "全部");
		dataMap1.put("value", "");
		options1.add(dataMap1);
		
		List<DictionaryValue> list1 = dictionaryOptionService.listByTypeOrderByTabIndexAsc("appScenarios");
		for(DictionaryValue value : list1) {
			Map<String, Object> rmap = new HashMap<String, Object>();
			rmap.put("label", value.getName());
			rmap.put("value", value.getCode());
			options1.add(rmap);
		}
		map1.put("options", options1);
		listMap.add(map1);
		
		// 数据专区
		Map<String, Object> map2 = new HashMap<String, Object>();
		map2.put("title", "数据专区");
		map2.put("key", "dataZone");
		
		List<Map<String, Object>> options2 = new ArrayList<Map<String,Object>>();
		Map<String, Object> dataMap2 = new HashMap<String, Object>();
		dataMap2.put("label", "全部");
		dataMap2.put("value", "");
		options2.add(dataMap2);
		
		List<DictionaryValue> list2 = dictionaryOptionService.listByTypeOrderByTabIndexAsc("dataZone");
		for(DictionaryValue value : list2) {
			Map<String, Object> rmap = new HashMap<String, Object>();
			rmap.put("label", value.getName());
			rmap.put("value", value.getCode());
			options2.add(rmap);
		}
		map2.put("options", options2);
		listMap.add(map2);
		
		// 产品类型
		Map<String, Object> map3 = new HashMap<String, Object>();
		map3.put("title", "产品类型");
		map3.put("key", "productType");
		
		List<Map<String, Object>> options3 = new ArrayList<Map<String,Object>>();
		Map<String, Object> dataMap3 = new HashMap<String, Object>();
		dataMap3.put("label", "全部");
		dataMap3.put("value", "");
		options3.add(dataMap3);
		
		List<DictionaryValue> list3 = dictionaryOptionService.listByTypeOrderByTabIndexAsc("productType");
		for(DictionaryValue value : list3) {
			Map<String, Object> rmap = new HashMap<String, Object>();
			rmap.put("label", value.getName());
			rmap.put("value", value.getCode());
			options3.add(rmap);
		}
		map3.put("options", options3);
		listMap.add(map3);
		
		return Y9Result.success(listMap);
	}

}
