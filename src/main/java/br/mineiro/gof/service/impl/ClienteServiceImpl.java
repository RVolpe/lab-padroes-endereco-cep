package br.mineiro.gof.service.impl;

import br.mineiro.gof.model.Cliente;
import br.mineiro.gof.model.ClienteRepository;
import br.mineiro.gof.model.Endereco;
import br.mineiro.gof.model.EnderecoRepository;
import br.mineiro.gof.service.ViaCepService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.mineiro.gof.service.ClienteService;

import java.util.Optional;

/**
 * Implementação da Strategy {ClienteService}, a qual pode ser injetada pelo Spring (via Autowired).
 * Com isso, como essa classe é um {Service}, ela será tratada como um Singleton.
 */

@Service
public class ClienteServiceImpl implements ClienteService {

    // Singleton: Injetar os componentes do Spring com @Autowired.
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private EnderecoRepository enderecoRepository;
    @Autowired
    private ViaCepService viaCepService;

    // Strategy: Implementar os métodos definidos na interface.
    // Facade: Abstrair integrações com subsistemas, provendo uma interface simples.

    @Override
    public Iterable<Cliente> buscarTodos() {
        // Buscar todos os Clientes.
        return clienteRepository.findAll();
    }

    @Override
    public Cliente buscarPorId(Long id) {
        // Buscar Cliente por ID.
        Optional<Cliente> cliente = clienteRepository.findById(id);
        return cliente.get();
    }

    @Override
    public void inserir(Cliente cliente) {
        // Inserir Cliente, vinculando o Endereco (novo ou existente).
        salvarClienteComCep(cliente);
    }

    @Override
    public void atualizar(Long id, Cliente cliente) {
        // Buscar Cliente por ID, caso exista:
        Optional<Cliente> clienteBD = clienteRepository.findById(id);
        if (clienteBD.isPresent()) {
            salvarClienteComCep(cliente);
        }
    }

    @Override
    public void deletar(Long id) {
        // Deletar Cliente por ID.
        clienteRepository.deleteById(id);
    }

    private void salvarClienteComCep(Cliente cliente) {
        // Verificar se o Endereco do Cliente já existe (pelo CEP, pois CEP é chave de endereço).
        String auxCep = cliente.getEndereco().getCep();

        Endereco endereco = enderecoRepository.findById(auxCep).orElseGet( () -> {
            // Caso não exista, integrar com o ViaCEP e persistir o retorno.
            Endereco novoEndereco = viaCepService.consultarCep(auxCep);
            enderecoRepository.save(novoEndereco);
            return novoEndereco;
        });

        cliente.setEndereco(endereco);

        // Inserir Cliente, vinculando o Endereco (novo ou existente).
        clienteRepository.save(cliente);
    }
}
