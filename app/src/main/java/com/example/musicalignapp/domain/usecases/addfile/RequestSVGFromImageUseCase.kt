package com.example.musicalignapp.domain.usecases.addfile

import com.example.musicalignapp.core.converters.jsonconverter.JsonConverter
import com.example.musicalignapp.data.core.NetworkError
import com.example.musicalignapp.di.InterfaceAppModule
import com.example.musicalignapp.domain.model.SVGRequestModel
import com.example.musicalignapp.domain.repository.AddFileRepository
import com.example.musicalignapp.ui.uimodel.SVGResponseUIModel
import com.example.musicalignapp.utils.AppError
import com.example.musicalignapp.utils.Result
import com.example.musicalignapp.utils.onError
import com.example.musicalignapp.utils.onSuccess
import javax.inject.Inject

class RequestSVGFromImageUseCase @Inject constructor(
    private val addFileRepository: AddFileRepository,
    @InterfaceAppModule.SVGFileConverterAnnotation private val svgFileConverter: JsonConverter,
) {

    suspend operator fun invoke(imageName: String): Result<List<SVGResponseUIModel>, AppError> {
        addFileRepository.requestSVGFromImage(SVGRequestModel(imageName))
            .onSuccess {
                val listSVGResponses: MutableList<SVGResponseUIModel> = mutableListOf()

                it.files.map { fileName ->
                    addFileRepository.getSvgContent(SVGRequestModel(fileName))
                        .onSuccess { content ->
                            val finalName = fileName.substringAfterLast('/')
                            val uri = svgFileConverter.createSVGFile(content, finalName)
                            listSVGResponses.add(SVGResponseUIModel(uri, finalName))
                        }
                        .onError { error ->
                            return Result.Error(error)
                        }
                }

                return Result.Success(listSVGResponses)
            }
            .onError {
                return Result.Error(it)
            }

        return Result.Error(NetworkError.UNKNOWN)
    }
}